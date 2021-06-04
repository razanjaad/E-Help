package com.e_help.Volunteer.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.e_help.Model.OpportunitiesModel;
import com.e_help.Model.User;
import com.e_help.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {
    View view;
    List<User> UserVolunteer = new ArrayList<>();
    int num_val = 0;
    int number_team = 0;
    int number_org = 0;
    int number_volunteer_opertunity=0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        TextView name1 = (TextView) view.findViewById(R.id.name1);
        TextView name2 = (TextView) view.findViewById(R.id.name2);
        TextView name3 = (TextView) view.findViewById(R.id.name3);

        TextView score1 = (TextView) view.findViewById(R.id.score1);
        TextView score2 = (TextView) view.findViewById(R.id.score2);
        TextView score3 = (TextView) view.findViewById(R.id.score3);

        TextView num_volunteer = (TextView) view.findViewById(R.id.num_volunteer);
        TextView num_volunteer_opertunity = (TextView) view.findViewById(R.id.num_volunteer_opertunity);
        TextView num_team = (TextView) view.findViewById(R.id.num_team);
        TextView num_org = (TextView) view.findViewById(R.id.num_org);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/")
                .getReference().child("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserVolunteer.clear();
                num_val = 0;
                number_team = 0;
                number_org = 0;
                if (dataSnapshot.exists())
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user.getUser_type() == 1) {
                            UserVolunteer.add(user);
                            num_val++;
                        } else if (user.getUser_type() == 2) {

                            number_team++;
                        } else if (user.getUser_type() == 3) {
                            number_org++;
                        }

                    }
                num_volunteer.setText(num_val + "");
                num_team.setText(number_team + "");
                num_org.setText(number_org + "");



                Collections.sort(UserVolunteer, new Comparator<User>(){
                    public int compare(User obj1, User obj2) {
                        return Integer.valueOf(obj2.getPoint()).compareTo(Integer.valueOf(obj1.getPoint())); // To compare integer values
                    }
                });
                if(UserVolunteer.size()>3){
                    name1.setText(UserVolunteer.get(0).getFirst_name()+" "+UserVolunteer.get(0).getLast_name());
                    name2.setText(UserVolunteer.get(1).getFirst_name()+" "+UserVolunteer.get(1).getLast_name());
                    name3.setText(UserVolunteer.get(2).getFirst_name()+" "+UserVolunteer.get(2).getLast_name());

                    score1.setText(UserVolunteer.get(0).getPoint()+"");
                    score2.setText(UserVolunteer.get(1).getPoint()+"");
                    score3.setText(UserVolunteer.get(2).getPoint()+"");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference mdatabaseOpportunities = FirebaseDatabase.getInstance("https://ehelp-24142-default-rtdb.firebaseio.com/").
                getReference().child("Opportunities");
        mdatabaseOpportunities.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                number_volunteer_opertunity=0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    OpportunitiesModel opportunitiesModel = snapshot.getValue(OpportunitiesModel.class);
                    number_volunteer_opertunity++;
                }

                num_volunteer_opertunity.setText(number_volunteer_opertunity+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
    }
}