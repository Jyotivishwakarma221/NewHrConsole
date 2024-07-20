package com.investmango.hrconsole.user.adapter.FirebaseService;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.investmango.hrconsole.model.LocationModel;

public class FirebaseConnection {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference myRef = database.getReference("AttendanceLocationCoordinates");

    public static void saveCoordinates(LocationModel locationModel, Long userId) {
        DatabaseReference newRef = myRef.child(String.valueOf(userId));
        newRef.setValue(locationModel);
    }

    public static void getCoordinate(Long userId) {
        Query query = myRef.child(String.valueOf(userId));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LocationModel o = dataSnapshot.getValue(LocationModel.class);
                Log.d(TAG, "Value is: " + o);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

}
