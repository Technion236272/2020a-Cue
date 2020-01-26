const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const firestoreTriggers = functions.region('europe-west1').firestore;
const db = admin.firestore()

// new appointment / change in existing appointment / cancellation of an appointment
exports.informOnAppointmentAndRemind = firestoreTriggers
                            .document('Businesses/{businessId}/AppointmentActions/{actionId}')
                            .onCreate((snap, context) => {

                                const businessId = context.params.businessId;

                                return db.collection('Businesses')
                                        .doc(businessId)
                                        .get()
                                        .then(business => {
                                            return db.collection('Clients')
                                            .where('name', '==', snap.data().client_name)
                                            .limit(1)
                                            .get()
                                            .then(function(querySnapshot) {
                                                const payload = {
                                                        data: {
                                                        action_type: snap.data().action_type,
                                                        business_name: business.data().business_name,
                                                        client_name: snap.data().client_name,
                                                        appointment_date: snap.data().appointment_date.toDate().getTime().toString(),
                                                        new_appointment_date: snap.data().new_appointment_date.toDate().getTime().toString(),
                                                        appointment_type: snap.data().appointment_type,
                                                        new_appointment_type: snap.data().new_appointment_type,
                                                        action_doer: snap.data().action_doer,
                                                        business_id: businessId,
                                                        notes: snap.data().notes
                                                    }
                                                };

                                                if (snap.data().action_doer === 'business') {
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
