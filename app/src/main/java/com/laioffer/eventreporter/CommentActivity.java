package com.laioffer.eventreporter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    private static final String TAG = "CommentActivity";

    private DatabaseReference mDatabaseReference;
    private RecyclerView mRecyclerView;
    private EditText mEditTextComment;
    private CommentAdapter commentAdapter;
    private Button mCommentSubmitButton;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        final String eventId = intent.getStringExtra("EventID");

        mRecyclerView = (RecyclerView) findViewById(R.id.comment_recycler_view);
        mEditTextComment = (EditText) findViewById(R.id.comment_edittext);
        mCommentSubmitButton = (Button) findViewById(R.id.comment_submit);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        commentAdapter = new CommentAdapter(this);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getData(eventId, commentAdapter);
    }

    private void getData(final String eventId, final CommentAdapter commentAdapter) {
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot commentSnapshot = dataSnapshot.child("comments");
                List<Comment> comments = new ArrayList<Comment>();
                for (DataSnapshot noteDataSnapshot : commentSnapshot.getChildren()) {
                    Comment comment = noteDataSnapshot.getValue(Comment.class);
                    if (comment.getEventId().equals(eventId)) {
                        comments.add(comment);
                    }
                }
                mDatabaseReference.getRef().child("events").child(eventId).
                        child("commentNumber").setValue(comments.size());
                commentAdapter.setComments(comments);

                DataSnapshot eventSnapshot = dataSnapshot.child("events");
                for (DataSnapshot noteDataSnapshot : eventSnapshot.getChildren()) {
                    Event event = noteDataSnapshot.getValue(Event.class);
                    if (event.getId().equals(eventId)) {
                        commentAdapter.setEvent(event);
                        break;
                    }
                }
                if (mRecyclerView.getAdapter() != null) {
                    commentAdapter.notifyDataSetChanged();
                } else {
                    mRecyclerView.setAdapter(commentAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
