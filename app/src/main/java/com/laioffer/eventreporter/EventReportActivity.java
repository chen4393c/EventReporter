package com.laioffer.eventreporter;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class EventReportActivity extends AppCompatActivity {
    private static final String TAG = "EventReportActivity";// EventReportActivity.class.getSimpleName();
    private EditText mEditTextLocation;
    private EditText mEditTextTitle;
    private EditText mEditTextContent;
    private ImageView mImageViewSend;
    private ImageView mImageViewCamera;
    private DatabaseReference database;

    private static int RESULT_LOAD_IMAGE = 1;
    private ImageView img_event_picture;
    private Uri mImgUri;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private LocationTracker mLocationTracker;

    // Set variables ready for uploading images
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_report);

        mEditTextLocation = (EditText) findViewById(R.id.edit_text_event_location);
        mEditTextTitle = (EditText) findViewById(R.id.edit_text_event_title);
        mEditTextContent = (EditText) findViewById(R.id.edit_text_event_content);
        mImageViewCamera = (ImageView) findViewById(R.id.img_event_camera);
        mImageViewSend = (ImageView) findViewById(R.id.img_event_report);
        database = FirebaseDatabase.getInstance().getReference();
        img_event_picture = (ImageView) findViewById(R.id.img_event_picture_capture);

        // Initialize cloud storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        /* Add click listener for the image to pick up images from gallery
        * through implicit intent */
        mImageViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
        mImageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = uploadEvent();
                if (mImgUri != null) {
                    uploadImage(key);
                    mImgUri = null;
                }
            }
        });

        /* User Authentication */
        mAuth = FirebaseAuth.getInstance();

        // Add listener to check sign in status
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Sign in anonymously
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInAnonymously:onComplete: " + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInAnonymously:onComplete: " + task.getException());
                }
            }
        });

        mLocationTracker = new LocationTracker(this);
        mLocationTracker.getLocation();
        final double latitude = mLocationTracker.getLatitude();
        final double longitude = mLocationTracker.getLongitude();
        Log.d(TAG, "latitude: " + latitude + ", " + "longitude: " + longitude);
        new AsyncTask<Void, Void, Void>() {
            private List<String> mAddressList = new ArrayList<>();

            @Override
            protected Void doInBackground(Void... voids) {
                mAddressList = mLocationTracker.getCurrentLocationViaJSON(latitude, longitude);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mAddressList.size() >= 3) {
                    mEditTextLocation.setText(
                            mAddressList.get(0) + ", " +
                                    mAddressList.get(1) + ", " +
                                    mAddressList.get(2) + ", " +
                                    mAddressList.get(3)
                    );
                }
                Log.d(TAG, "mAddressList: " + mAddressList);
            }
        }.execute();


    }

    /**
     * Gather information inserted by user and create event for uploading. Then clear those widgets if user uploads one

     * @return the key of the event needs to be returned as link against Cloud storage
     */

    private String uploadEvent() {
        String title = mEditTextTitle.getText().toString();
        String location = mEditTextLocation.getText().toString();
        String description = mEditTextContent.getText().toString();
        if (location.equals("") || description.equals("") ||
                title.equals("") || Utils.username == null) {
            return null;
        }
        //create event instance
        Event event = new Event();
        event.setTitle(title);
        event.setAddress(location);
        event.setDescription(description);
        event.setTime(System.currentTimeMillis());
        event.setLatitude(mLocationTracker.getLatitude());
        event.setLongitude(mLocationTracker.getLongitude());

        event.setUsername(Utils.username);
        String key = database.child("events").push().getKey();
        event.setId(key);
        database.child("events").child(key).setValue(event,
                new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast toast = Toast.makeText(
                            getBaseContext(),
                            "The event is failed, please check your network status.",
                            Toast.LENGTH_SHORT
                    );
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(
                            getBaseContext(),
                            "The event is reported",
                            Toast.LENGTH_SHORT
                    );
                    toast.show();
                    mEditTextTitle.setText("");
                    mEditTextLocation.setText("");
                    mEditTextContent.setText("");
                }
            }
        });
        return key;
    }

    /*
    * Send intent to launch gallery for us to pick up images, once the action finishes, images will be returned as parameters
    * in this function.
    * @param requestCode code for intent to start gallery activity
    * @param resultCode result code returned when finishing picking up images from gallery
    * @param data content returned from gallery, including images we picked
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
                Uri seletedImage = data.getData();
                img_event_picture.setVisibility(View.VISIBLE);
                mImgUri = seletedImage;
                img_event_picture.setImageURI(mImgUri);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Upload image picked up from gallery to Firebase Cloud storage
     * @param eventId eventId
     */
    private void uploadImage(final String eventId) {
        if (mImgUri == null) {
            return;
        }
        StorageReference imgRef = storageRef.child("images/" + mImgUri.getLastPathSegment() + "_"
                + System.currentTimeMillis());

        UploadTask uploadTask = imgRef.putFile(mImgUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.i(TAG, "upload successfully" + eventId);
                database.child("events").child(eventId).child("imgUri").
                        setValue(downloadUrl.toString());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
