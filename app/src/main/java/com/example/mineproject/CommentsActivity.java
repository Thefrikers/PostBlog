package com.example.mineproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentsActivity extends AppCompatActivity {

    EditText comment_field;
    ImageView comment_post_btn;
    String blog_post_id,current_user_id;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    RecyclerView comment_list;
    CommentsRecyclerAdapter commentsRecyclerAdapter;
    List<Comments> commentsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_comments);


        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        current_user_id=firebaseAuth.getCurrentUser().getUid();

        blog_post_id=getIntent().getStringExtra("blog_post_id");


      comment_field=findViewById(R.id.comment_field);
      comment_post_btn=findViewById(R.id.comment_post_btn);

      comment_list =findViewById(R.id.comment_list);


      commentsList=new ArrayList<>();
      commentsRecyclerAdapter=new CommentsRecyclerAdapter(commentsList);

      comment_list.setHasFixedSize(true);
      comment_list.setLayoutManager(new LinearLayoutManager(this));
      comment_list.setAdapter(commentsRecyclerAdapter);



        Query firstQuery=firebaseFirestore.collection("Posts/" + blog_post_id +"/Comments").orderBy("timestamp",Query.Direction.ASCENDING);

     firstQuery.addSnapshotListener(CommentsActivity.this,new EventListener<QuerySnapshot>() {
          @Override
          public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

              if(!queryDocumentSnapshots.isEmpty())
              {
                  for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges())
                  {
                      if (doc.getType()==DocumentChange.Type.ADDED)
                      {
                          String commentId=doc.getDocument().getId();
                          Comments comments=doc.getDocument().toObject(Comments.class);
                          commentsList.add(comments);
                          commentsRecyclerAdapter.notifyDataSetChanged();


                      }
                  }
              }


          }
      });



      comment_post_btn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              String comment_message=comment_field.getText().toString();

              if (!comment_message.isEmpty())
              {
                  Map<String,Object> commentsMap=new HashMap<>();
                  commentsMap.put("message",comment_message);
                  commentsMap.put("user_id",current_user_id);
                  commentsMap.put("timestamp", FieldValue.serverTimestamp());


                  firebaseFirestore.collection("Posts/"+ blog_post_id +"/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                      @Override
                      public void onComplete(@NonNull Task<DocumentReference> task) {

                          if (!task.isSuccessful())
                          {

                          }
                          else
                          {
                              comment_field.setText("");
                          }


                      }
                  });



              }

          }
      });


    }
}
