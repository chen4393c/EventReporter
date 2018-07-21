const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
//exports.helloWorld = functions.https.onRequest((request, response) => {
// response.send("Hello from Firebase!");
//});

var admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

// Listen to changes on database
exports.sendNotification = functions.database.ref('/events/{pushId}')
    .onWrite((change, context) => {
        // Grab the current value of what was written to the Realtime Database
        var eventSnapshot = change.after.val();
        var topic = "android";
        var payload = {
            data: {
                id: eventSnapshot.id,
                title: eventSnapshot.title,
                description: eventSnapshot.description,
                address: eventSnapshot.address,
                imgUri: eventSnapshot.imgUri
            }
        };

        // Send a message to devices subscribed to the provided topic
        return admin.messaging().sendToTopic(topic, payload)
            .then(function(response) {
                // See the MessagingTopicResponse reference documentation for
                // the content of response.
                console.log("Successfully sent message: ", response);
                return -1;
            }).catch(function(error) {
                console.log("Error sending message: ", error);
            });
    });
