package com.e_help.Team;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.e_help.Model.MemberModel;
import com.e_help.Model.TaskModel;
import com.e_help.Model.User;
import com.e_help.Model.dataModel2;
import com.e_help.R;
import com.e_help.User.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    EditText title, description, date;
    DatabaseReference mdatabaseTeamMembers,referenceTask;
    String Uid;
    ProgressDialog dialogM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        Uid = preferences.getString("Uid", "");
        dialogM = new ProgressDialog(this);
        dialogM.setMessage("جاري الحفظ يرجى الانتظار ...");
        dialogM.setIndeterminate(true);
        findViewById(R.id.arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        date = (EditText) findViewById(R.id.date);
        getTime(date);
        mdatabaseTeamMembers = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/").
                getReference().child("TeamMembers").child(Uid);
        referenceTask = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/")
                .getReference().child("Task").child(Uid);
        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validation()) {
                    dialogM.show();
                    String key = referenceTask.push().getKey();
                    TaskModel taskModel = new TaskModel(key, Uid, title.getText().toString(),
                            description.getText().toString(), date.getText().toString(), selectedNameUser, selectedUser);
                    referenceTask.child(key).setValue(taskModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialogM.dismiss();
                            Toast.makeText(AddTaskActivity.this, "تمت الاضافة بنجاح", Toast.LENGTH_SHORT).show();
                            title.setText("");
                            description.setText("");
                            date.setText("");
                        }
                    });
                }
            }
        });
        GetUser(); }

    public boolean validation() {
        if (TextUtils.isEmpty(title.getText().toString().trim())) {
            Toast.makeText(AddTaskActivity.this, "أدخل العنوان", Toast.LENGTH_SHORT).show();
            title.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(description.getText().toString().trim())) {
            Toast.makeText(AddTaskActivity.this, "أدخل الوصف", Toast.LENGTH_SHORT).show();
            description.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(date.getText().toString().trim())) {
            Toast.makeText(AddTaskActivity.this, "أدخل موعد الانتهاء", Toast.LENGTH_SHORT).show();
            date.requestFocus();
            return false;
        }return true; }
    String selectedUser = "0";
    String selectedNameUser = "";
    public void GetUser() {
        Spinner userSp = (Spinner) findViewById(R.id.sp_user);
        List<dataModel2> listModels = new ArrayList<>();
        listModels.add(new dataModel2("0", "تعيين الجميع"));
        mdatabaseTeamMembers.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        MemberModel user = snapshot.getValue(MemberModel.class);
                        if (user.isAccepted())
                            listModels.add(new dataModel2(user.getId_user(), user.getFname_user() + " " + user.getLname_user()));


                    }
                }
                final String[] name = new String[listModels.size()];
                final String[] idList = new String[listModels.size()];

                for (int i = 0; i < listModels.size(); i++) {
                    name[i] = listModels.get(i).getName();
                    idList[i] = listModels.get(i).getId() + "";
                }

                ArrayAdapter adapterPiece = new ArrayAdapter<String>(AddTaskActivity.this, android.R.layout.simple_spinner_dropdown_item, name);
                userSp.setAdapter(adapterPiece);
                userSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Object item = parent.getItemAtPosition(position);
                        try {
                            ((TextView) view).setTextColor(Color.BLACK);
                        } catch (Exception ex) {
                        }
                        if (item != null) {
                            selectedUser = idList[position];
                            selectedNameUser = name[position];
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddTaskActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }
    public void getTime(final EditText editText) {

        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddTaskActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                date.set(year, monthOfYear, dayOfMonth);
                String myFormat = "dd-MM-yyyy";// HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                editText.setText(sdf.format(date.getTime()));
                editText.setError(null);

            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE));
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                datePickerDialog.show();
            }
        });
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());

    }

}