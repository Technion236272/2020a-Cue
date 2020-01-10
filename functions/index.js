const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const firestoreTriggers = functions.region('europe-west1').firestore;
const db = admin.firestore()

// new appointment / change in exisiting appointment / cancellation of an appointment
exports.informOnAppointmentAndRemind = firestoreTriggers
                            .document('Businesses/{businessId}/AppointmentActions/{actionId}')
                            .onWrite((snap, context) => {

                                const businessId = context.params.businessId;

                                return db.collection('Businesses')
                                        .doc(businessId)
                                        .get()
                                        .then(business => {
                                            return db.collection('Clients')
                                            .where('name', '==', snap.after.data().client_name)
                                            .limit(1)
                                            .get()
                                            .then(function(querySnapshot) {
                                                const payload = {
                                                        data: {
                                                        action_type: snap.after.data().action_type,
                                                        business_name: business.data().business_name,
                                                        client_name: snap.after.data().client_name,
                                                        appointment_date: snap.after.data().appointment_date.toDate().getTime().toString(),
                                                        new_appointment_date: snap.after.data().new_appointment_date.toDate().getTime().toString(),
                                                        appointment_type: snap.after.data().appointment_type,
                                                        action_doer: snap.after.data().action_doer
                                                    }
                                                };

                                                if (snap.after.data().action_doer === 'client') {
                                                    console.log('Sending to client: ' + querySnapshot.docs[0].id)
                                                    return admin.messaging().sendToTopic(querySnapshot.docs[0].id, payload)  
                                                } else {
                                                    console.log('Sending to business: ' + businessId)
                                                    return admin.messaging().sendToTopic(businessId, payload)  
                                                }

                                            });
                                        });
                                });

// new review trigger
exports.informOnNewReview = firestoreTriggers
                            .document('Businesses/{businessId}/Reviews/{reviewId}')
                            .onCreate((snap, context) => {

                                const businessId = context.params.businessId;

                                const payload = {
                                    data: {}
                                };

                                return admin.messaging().sendToTopic(businessId, payload) 

});
