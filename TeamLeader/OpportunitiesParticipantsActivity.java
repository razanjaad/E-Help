package com.e_help.Team;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e_help.Adapter.OpportunitiesParticipantsAdapter;
import com.e_help.Model.OpportunitieRegisterModel;
import com.e_help.Notification.APIService;
import com.e_help.Notification.Notifications.Client;
import com.e_help.Notification.Notifications.Data;
import com.e_help.Notification.Notifications.MyResponse;
import com.e_help.Notification.Notifications.Sender;
import com.e_help.Notification.Notifications.Token;
import com.e_help.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpportunitiesParticipantsActivity extends AppCompatActivity {
    List<OpportunitieRegisterModel> resultsList;
    OpportunitiesParticipantsAdapter nAdapter;
    RecyclerView recyclerView;
    ProgressBar progress_bar;
    DatabaseReference mdatabase,mdatabaseUser;
    TextView no_data;
    ProgressDialog dialog1;
    String ID = "";
    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunities_participants);
        //للربط مع الفيربيس للاشعارات
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        dialog1 = new ProgressDialog(this);
        dialog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog1.setMessage("الرجاء الانتظار ...");
        dialog1.setIndeterminate(true);
        dialog1.setCanceledOnTouchOutside(false);
        findViewById(R.id.noti_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog myQuittingDialogBox = new AlertDialog.Builder(OpportunitiesParticipantsActivity.this)
                        // set message, title, and icon
                        .setTitle("اشعار الجميع")
                        .setMessage("هل انت متأكد من اشعار الجميع؟")
                        .setPositiveButton("تأكيد", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //your deleting code
                                dialog.dismiss();
                                dialog1.show();
                                for (int i = 0; i < resultsList.size(); i++) {
                                    if (resultsList.get(i).isConfirmAttendance()) {
                                        sendNotification(resultsList.get(i).getId_member(),
                                                "تم قبول طلبك بنجاح");
                                        dialog1.dismiss();
                                        Toast.makeText(OpportunitiesParticipantsActivity.this, "تم الارسال", Toast.LENGTH_SHORT).show();

                                    }
                                }


                            }

                        })
                        .setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        })
                        .create();
                myQuittingDialogBox.show();


            }
        });
        Intent intent = getIntent();
        ID = intent.getStringExtra("ID");
        mdatabase = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/").getReference().
                child("OpportunitiesRegister").child(preferences.getString("Uid", "")).child(ID);
        mdatabaseUser = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/").getReference().
                child("Users");
        no_data = findViewById(R.id.no_data);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        recyclerView = findViewById(R.id.recycler);
        resultsList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        nAdapter = new OpportunitiesParticipantsAdapter(this, resultsList);
        recyclerView.setAdapter(nAdapter);


        progress_bar.setVisibility(View.VISIBLE);
        findViewById(R.id.accepted_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog myQuittingDialogBox = new AlertDialog.Builder(OpportunitiesParticipantsActivity.this)
                        // set message, title, and icon
                        .setTitle("تأكيد حضور الجميع")
                        .setMessage("هل انت متأكد من حضور الجميع؟")
                        .setPositiveButton("تأكيد", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //your deleting code
                                dialog.dismiss();
                                dialog1.show();
                                for (int i = 0; i < resultsList.size(); i++) {
                                    if (resultsList.get(i).isConfirmAttendance()) {

                                        int finalI = i;
                                        mdatabase.child(resultsList.get(i).getId_member())
                                                .child("accepted").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                dialog1.dismiss();
                                                Toast.makeText(OpportunitiesParticipantsActivity.this, "تم", Toast.LENGTH_SHORT).show();
                                                //لزيادة عدد النقاط على حسب عدد ساعات التطوع
                                                mdatabaseUser.child(resultsList.get(finalI).getId_member())
                                                        .child("point").runTransaction(new Transaction.Handler() {
                                                    @Override
                                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                                        Integer score = mutableData.getValue(Integer.class);
                                                        if (score == null) {
                                                            return Transaction.success(mutableData);
                                                        }
                                                        mutableData.setValue(score + resultsList.get(finalI).getNum_h());

                                                        return Transaction.success(mutableData);
                                                    }

                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }


                            }

                        })
                        .setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        })
                        .create();
                myQuittingDialogBox.show();
            }
        });


        mdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                resultsList.clear();
                progress_bar.setVisibility(View.GONE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    OpportunitieRegisterModel opportunitiesModel = snapshot.getValue(OpportunitieRegisterModel.class);
                    if (opportunitiesModel.isConfirmAttendance()) {
                        resultsList.add(opportunitiesModel);
                        nAdapter.notifyDataSetChanged();
                    }
                    if (!opportunitiesModel.isAccepted()) {
                        findViewById(R.id.accepted_L).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.accepted_L).setVisibility(View.GONE);
                    }
                }
                if (resultsList.size() == 0) {
                    no_data.setVisibility(View.VISIBLE);
                } else {
                    no_data.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
                resultsList.clear();
                nAdapter.notifyDataSetChanged();
                recyclerView.removeAllViews();
                if (resultsList.size() == 0) {
                    no_data.setVisibility(View.VISIBLE);
                } else {
                    no_data.setVisibility(View.GONE);
                }
                Toast.makeText(OpportunitiesParticipantsActivity.this, "no data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendNotification(String receiver, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/").getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

                    Data data = new Data(fuser.getUid(), "" + message, "Ehelp");

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}