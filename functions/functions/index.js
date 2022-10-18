const functions = require("firebase-functions");

const admin = require("firebase-admin")
admin.initializeApp()

exports.notifyNewMessage = functions.firestore
                .document("chatChannels/{channel}/messages/{message}")
                .onCreate((docSnapShot , context)=>{
                    const message = docSnapShot.data()
                    const receiverId = message["receiverId"]
                    const senderName = message["senderName"]

                    return admin.firestore().doc('Users/'+receiverId).get().then(userDoc =>{
                        const registrationTokens = userDoc.get("registrationTokens")
                        const notificationBody = (message['type'] === "TEXT") ? message['text'] : "You received a new image message."
                        const payload = {
                            notification:{
                                title: senderName+" sent you a message." ,
                                body: notificationBody ,
                                USER_NAME: senderName ,
                            }
                        }

                        return admin.messaging().sendToDevice(registrationTokens , payload).then(response=>{
                            const stillRegisteredTokens = registrationTokens

                            response.results.forEach((result , index)=>{
                                const error = result.error

                                if(error){
                                    const failedRegistrationToken = registrationTokens[index]
                                    console.error("error" , failedRegistrationToken)
                                    if(error.code === 'messaging/invalid-registration-token' || 
                                       error.code === 'messaging/registration-token-not-registered'){
                                        const failedIndex = stillRegisteredTokens.indexOf(failedRegistrationToken)
                                        if(failedIndex > -1){
                                            stillRegisteredTokens.splice(failedIndex , 1)
                                        }
                                       }
                                }
                            })


                            return admin.firestore().doc("Users/"+receiverId).update({
                                registrationTokens: stillRegisteredTokens
                            })

                        })

                    })
                })



