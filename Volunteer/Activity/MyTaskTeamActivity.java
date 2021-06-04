package com.e_help.Volunteer.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e_help.Adapter.TaskVolunteerAdapter;
import com.e_help.Adapter.TeamMembersAdapter;
import com.e_help.Model.MemberModel;
import com.e_help.Model.TaskModel;
import com.e_help.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyTaskTeamActivity extends AppCompatActivity {
    List<TaskModel> resultsList;
    TaskVolunteerAdapter nAdapter;
    RecyclerView recyclerView;
    ProgressBar progress_bar;
    TextView no_data;
    String Uid;
    List<String> listTeams = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_task);

        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        Uid = preferences.getString("Uid", "");

        findViewById(R.id.arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        resultsList = new ArrayList<>();

        no_data = findViewById(R.id.no_data);
        recyclerView = findViewById(R.id.recycler);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        nAdapter = new TaskVolunteerAdapter(this, resultsList);
        recyclerView.setAdapter(nAdapter);

        progress_bar.setVisibility(View.VISIBLE);
        DatabaseReference mdatabaseTask = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/").
                getReference().child("Task");
        DatabaseReference mdatabaseTeamMembers = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/").
                getReference().child("TeamMembers");
        //لجلب الفرق التي تم المشاركة فيها
        mdatabaseTeamMembers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MemberModel memberModel = snapshot1.getValue(MemberModel.class);
                            if (memberModel.getId_user().equals(Uid)) {
                                listTeams.add(memberModel.getId_team());
                            }
                        }
                    }
                    //لالغاء التكرار
                    Set<String> set = new HashSet<>(listTeams);
                    listTeams.clear();
                    listTeams.addAll(set);
                    resultsList.clear();
                    //لعرض المهام
                    for (int i = 0; i < listTeams.size(); i++) {
                        mdatabaseTask.child(listTeams.get(i)).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    progress_bar.setVisibility(View.GONE);
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        TaskModel user = snapshot.getValue(TaskModel.class);
                                        //لتأكد من ان المهمة للمتطوع اول للجميع
                                        if (user.getUser_id().equals(Uid) || user.getUser_id().equals("0")) {
                                            resultsList.add(user);
                                            nAdapter.notifyDataSetChanged();

                                        }

                                    }
                                    if (resultsList.size() == 0) {
                                        no_data.setVisibility(View.VISIBLE);
                                    } else {
                                        no_data.setVisibility(View.GONE);
                                    }

                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MyTaskTeamActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                progress_bar.setVisibility(View.GONE);
                                resultsList.clear();
                                nAdapter.notifyDataSetChanged();
                                recyclerView.removeAllViews();
                            }
                        });
                    }


                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MyTaskTeamActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }
}