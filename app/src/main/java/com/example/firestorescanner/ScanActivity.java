package com.example.firestorescanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorescanner.AnalysisModels.AnalysisModel;
import com.example.firestorescanner.AnalysisModels.Details;
import com.example.firestorescanner.AnalysisModels.Entries_Model;
import com.example.firestorescanner.AnalysisModels.Entry_Model;
import com.example.firestorescanner.AnalysisModels.Gate_map;
import com.example.firestorescanner.AnalysisModels.Temp;
import com.example.firestorescanner.AnalysisModels.Time_map;
import com.example.firestorescanner.AnalysisModels.Today;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.example.firestorescanner.Helper.getValueFromSharedPreferences;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, Constants{

    private ZXingScannerView mScannerView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String Institution_Path, Device_Name, actual_Position;
    String scannedId= "";
    public static final String TAG = logCatTag_Activity_SCAN;

//------------------------------------showScannedIdDetailsDialog()----------------------------
    TextView txt_layout_dialog_scanned_id_details_id, txt_layout_dialog_scanned_id_details_Name,
            txt_layout_dialog_scanned_id_details_Gender, txt_layout_dialog_scanned_id_details_Age;
    ImageView img_layout_dialog_scanned_id_details_profile_picture;
    Bitmap profile_picture;
    View alertLayout;
    AlertDialog.Builder alert;

    AnalysisModel analysisModel = new AnalysisModel();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        Log.d(TAG, "onCreate()");
        //check_If_SP_exists() not required.
            Institution_Path = getValueFromSharedPreferences(ScanActivity.this, KEY_institution_path).toString();
            Device_Name = getValueFromSharedPreferences(ScanActivity.this, KEY_device_name).toString();
            actual_Position = getValueFromSharedPreferences(ScanActivity.this, KEY_actual_position).toString();

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
        mScannerView.setAutoFocus(true);
        mScannerView.setSquareViewFinder(true);

        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScannerView.stopCamera();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        finish();
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.d(TAG, "ScanActivity: handleResult() " + rawResult.getText()); // Prints scan results
        // Log.v("tag", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        mScannerView.stopCameraPreview();
        scannedId = rawResult.getText();

        ScanActivity_Helper scanActivity_helper = new ScanActivity_Helper(rawResult.getText());
        scanActivity_helper.execute();
    }


    public  class ScanActivity_Helper extends AsyncTask<Void, Void, Void> {

        public String scannedId;

        public ScanActivity_Helper(String scannedId) {
            this.scannedId = scannedId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            addEntry(scannedId);
            return null;
        }
    }

    public void addEntry(final String scanned_Id )
    {
        Log.d(TAG, "addEntry()");
        db.document(Institution_Path + firestore_ids+"/" + scanned_Id )
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            if (Objects.requireNonNull(task.getResult()).exists()) {
                                Boolean isBlocked=task.getResult().getBoolean(firestore_IDS_FIELD_Blocked);
                                if(!isBlocked){
                                    playSound(true);
                                    System.out.println("isFromCache: "+task.getResult().getMetadata().isFromCache());
                                    mScannerView.setFlash(true);

                                    addEntry();
                                    incrementDeviceScanCount();   //TODO: updates counter for both in and out change it as needed.

                                    //TODO: display id details for two seconds.
                                    showScannedIdDetailsDialog(task, scanned_Id);

                                    mScannerView.setFlash(false);
                                }else{
                                    Toast.makeText(ScanActivity.this, "Following ID is blocked by administrator: " + scanned_Id, Toast.LENGTH_LONG).show();
                                    playSound(false);
                                }
                            }else {
                                Toast.makeText(ScanActivity.this, "ID does not exists.", Toast.LENGTH_LONG).show();
                                playSound(false);
                            }
                        }
                         else {
                            Toast.makeText(ScanActivity.this, "Entry update not successful.", Toast.LENGTH_LONG).show();
                            playSound(false);
                         }
                         mScannerView.resumeCameraPreview(ScanActivity.this);
                    }
                });
    }

    private void showScannedIdDetailsDialog(@NonNull Task<DocumentSnapshot> task, String scanned_Id) {
//        TextView txt_layout_dialog_scanned_id_details_id, txt_layout_dialog_scanned_id_details_Name,
//                txt_layout_dialog_scanned_id_details_Gender, txt_layout_dialog_scanned_id_details_Age;
//        ImageView img_layout_dialog_scanned_id_details_profile_picture;

        alertLayout = getLayoutInflater().inflate(R.layout.layout_dialog_scanned_id_details,null);
        txt_layout_dialog_scanned_id_details_id = alertLayout.findViewById(R.id.txt_layout_dialog_scanned_id_details_id);
        txt_layout_dialog_scanned_id_details_Name = alertLayout.findViewById(R.id.txt_layout_dialog_scanned_id_details_Name);
        txt_layout_dialog_scanned_id_details_Age = alertLayout.findViewById(R.id.txt_layout_dialog_scanned_id_details_Age);
        txt_layout_dialog_scanned_id_details_Gender = alertLayout.findViewById(R.id.txt_layout_dialog_scanned_id_details_Gender);
        img_layout_dialog_scanned_id_details_profile_picture = alertLayout.findViewById(R.id.img_layout_dialog_scanned_id_details_profile_picture);

        txt_layout_dialog_scanned_id_details_id.setText(scanned_Id);
        txt_layout_dialog_scanned_id_details_Name.setText(task.getResult().getString(firestore_IDS_FIELD_Name));
        txt_layout_dialog_scanned_id_details_Age.setText(String.valueOf(task.getResult().getLong(firestore_IDS_FIELD_Age)));
        txt_layout_dialog_scanned_id_details_Gender.setText(task.getResult().getString(firestore_IDS_FIELD_Gender));

        profile_picture = getDecodedImage(task.getResult().getString(firestore_IDS_FIELD_Image));
        if(profile_picture!= null)
            img_layout_dialog_scanned_id_details_profile_picture.setImageBitmap(profile_picture);


        alert = new AlertDialog.Builder(ScanActivity.this);
        alert.setView(alertLayout);
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();
        dialog.show();
        mScannerView.stopCamera();
        // Hide after some seconds
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.startCamera();
                mScannerView.resumeCameraPreview(ScanActivity.this);
                dialog.dismiss();
            }
        }, 3000);
    }

    public Bitmap getDecodedImage(String byteString)
    {
        if(byteString==null)
            return null;
        byte[] imageBytes = Base64.decode(byteString, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        return decodedImage;
    }

    public String convert_Date_To(String convert_to, Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if(convert_to.equals("DAY"))
            return DateFormat.format("EEEE", date).toString();      //TODO: 15 August shifted to friday instead of thursday.
        else if(convert_to.equals("YEAR"))
            return String.valueOf(calendar.get(Calendar.YEAR));
        else if(convert_to.equals("MONTH"))
            return String.valueOf(calendar.get(Calendar.MONTH));
        else if(convert_to.equals("DATE"))
            return String.valueOf(calendar.get(Calendar.DATE));
        return null;
    }

    private void addEntry() {
        final String path = Institution_Path + firestore_analysis_to_Year + "/" + convert_Date_To("YEAR",new Date()) + "/" + getMonthName(Integer.parseInt(convert_Date_To("MONTH",new Date())));
        db.document(path)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists() && documentSnapshot.getData().get(firestore_ANALYSIS_FIELD_DATA)!=null) {
                                analysisModel = documentSnapshot.toObject(AnalysisModel.class);
                                Today today;
                                //TODO: check if this for loop is required or not.
                            for (int i = 0; i < analysisModel.getData().size(); i++) {
                                    today = analysisModel.getData().get(i);
                                    Date todayDate = today.getDate();
                                    if(convert_Date_To("DATE",new Date()) == convert_Date_To("DATE", todayDate)){
                                        //TODO: Same Day, find Id and add Entry.
                                        analysisModel.getData().set(i, andEntryToSameDay(today));
                                        break;
                                    }else if(i == (analysisModel.getData().size()-1)){
                                        //TODO: Its a new Day add new Today to List<Today>.
                                        analysisModel.getData().add(addEntryToNewDay());
                                    }
                                }
                        } else {
                                //TODO: add first entry in List<Today>.
                            List<Today> todayList = new ArrayList<>();
                            todayList.add(addEntryToNewDay());
                            analysisModel.setData(todayList);
                        }

                        addAnalysisModel_To_Firestore(path, analysisModel);
                    }
                });
    }

    public void addAnalysisModel_To_Firestore(String path, AnalysisModel analysisModel){

        db.document(path )
                .set(analysisModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(ScanActivity.this, "Analysis updated successfully.", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "Analysis updated successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(ScanActivity.this, "ERROR" +e.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                    }
                });
    }

    private Today addEntryToNewDay() {
        Gate_map gate_map = new Gate_map(actual_Position, "Still In"); //TODO: make gate dynamic.
        Time_map time_map = new Time_map(new Date(), getZeroTimeDate(new Date()));

        Entry_Model entry_model = new Entry_Model("In", gate_map, time_map);

        List<Entry_Model> entryModelList = new ArrayList<>();
        entryModelList.add(entry_model);
        Details details = new Details(entryModelList);

        Entries_Model entries_model = new Entries_Model(Long.parseLong("1"), Long.parseLong(scannedId), details);

        List<Entries_Model> entries = new ArrayList<>();
        entries.add(entries_model);

        Today newtoday = new Today(new Date(), entries);
        return newtoday;
    }

    private Today andEntryToSameDay(Today today) {

        Entry_Model entry_model;
        Gate_map gate_map;
        Time_map time_map;

        for (Entries_Model entries : today.getEntries()) {

            if (entries.getId() == Long.parseLong(scannedId))        // id entry does exists.
            {
                //add an entry and update count.
                int current_index = today.getEntries().indexOf(entries);    //In below.
                if (today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() - 1).getStatus().equals("Out")) {
                    today.getEntries().get(current_index).setCount(today.getEntries().get(current_index).getCount() + 1);

                    //TODO: add new In entry to details.
                    gate_map = new Gate_map(actual_Position, "Still In");    //TODO: make gate dynamic.
                    time_map = new Time_map(new Date(), getZeroTimeDate(new Date()));

                    entry_model = new Entry_Model("In", gate_map, time_map);
                    today.getEntries().get(current_index).getDetails().getEntry().add(entry_model);
                    break;
                }   //Out below.
                else if (today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() - 1).getStatus().equals("In")) {

                    today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() - 1).setStatus("Out");
                    today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() - 1).getGate().setExit_Gate(actual_Position); //TODO: make gate dynamic.
                    today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() - 1).getTime().setExit_Time(new Date());
                    break;
                }// id entry does not exists.
            } else if ((entries.getId() != Long.parseLong(scannedId))) {

                if (today.getEntries().indexOf(entries) == (today.getEntries().size() - 1)) {
                    gate_map = new Gate_map(actual_Position, "Still In");    //TODO: make gate dynamic.
                    time_map = new Time_map(new Date(), getZeroTimeDate(new Date()));

                    entry_model = new Entry_Model("In", gate_map, time_map);

                    List<Entry_Model> entryModelList = new ArrayList<>();
                    entryModelList.add(entry_model);
                    Details details = new Details(entryModelList);

                    Entries_Model entries_model = new Entries_Model(Long.parseLong("1"), Long.parseLong(scannedId), details);
                    today.getEntries().add(entries_model);
                }
            }
        }
        //TODO: overwrite today with current data.
        return today;
    }

    public String getMonthName(int month){
        //TODO: january is 0 not 1.
        switch(month){
            case 0: {return ("January"); }
            case 1: {return ("February"); }
            case 2: {return ("March"); }
            case 3: {return ("April"); }
            case 4: {return ("May"); }
            case 5: {return ("June"); }
            case 6: {return ("July"); }
            case 7: {return ("August"); }
            case 8: {return ("September"); }
            case 9: {return ("October"); }
            case 10:{return ("November"); }
            case 11:{return ("December"); }
        }
        return "Wrong Month Number";
    }

    private Date getZeroTimeDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        //Toast.makeText(ScanActivity.this, date.toString(), Toast.LENGTH_LONG).show();
        Log.d("TAG", date.toString());
        return date;
    }

    public void incrementDeviceScanCount()
    {
        Map<String,Object> count = new HashMap<>();
  //      if(isEntryDoc_exists_already)
            count.put("Scans Today",FieldValue.increment(1));
  //      else
  //          count.put("Total Scans Today", Long.parseLong("1"));

        db.document(Institution_Path + firestore_device +"/" + Device_Name)  //TODO: change Redmi Note 3 to Device Name stored in SP.
                    .update(count)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Toast.makeText(ScanActivity.this ,"Incremented Scan Count.", Toast.LENGTH_SHORT).show();
                            Log.d("myCount","Incremented Scan Count.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Toast.makeText(ScanActivity.this ,"Unable to increment Scan Count.\nError: " + e.toString(), Toast.LENGTH_SHORT).show();
                            Log.d("myCount","Unable to increment Scan Count.\nError: " + e.toString());
                            }
                });
    }

    public void playSound(Boolean correct)
    {
        MediaPlayer mp = null;
        if(correct)
            mp = MediaPlayer.create(ScanActivity.this,R.raw.correct);
        else if(!correct)
            mp = MediaPlayer.create(ScanActivity.this,R.raw.incorrect);

        mp.start();
    }

    //TODO: methods to be removed.
    private Temp addEntry_Analysis(String status) {
        Gate_map gate_map = new Gate_map("Gate 1", "Gate 1");
        Time_map time_map = new Time_map(new Date(), new Date());

        Entry_Model entry_model = new Entry_Model(status, gate_map, time_map);

        List<Entry_Model> entryModelList = new ArrayList<>();
        entryModelList.add(entry_model);
        entryModelList.add(entry_model);
        Details details = new Details(entryModelList);

        Entries_Model entries_model = new Entries_Model(Long.parseLong("0"),Long.parseLong(scannedId), details);

        List<Entries_Model> entries = new ArrayList<>();
        entries.add(entries_model);
        entries.add(entries_model);
        entries.add(entries_model);

        Today today = new Today(new Date(),entries);
        Map<String, Today> today_hashMap = new HashMap<>();
        today_hashMap.put("Today",today);
        return new Temp(today_hashMap);
    }

    public  void clear_Fields(String path, List<String> fields)
    {
        Map<String,Object> fields_to_be_deleted = new HashMap<>();
        for(String field : fields)
            fields_to_be_deleted.put(field, FieldValue.delete());
        db.document(path)
                .update(fields_to_be_deleted)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ScanActivity.this, "Fields deleted successfully.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this, "ERROR" +e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                    }
                });
    }

    public Task<String> insertEntry(String path, QuerySnapshot querySnapshot)
    {
        String status;
        if(!querySnapshot.isEmpty()) {
            status = querySnapshot.getDocuments().get(querySnapshot.getDocuments().size() - 1).getString("Status");
            Toast.makeText(ScanActivity.this, "Status: " + status,
                    Toast.LENGTH_SHORT).show();
            if(status.equals("In"))
                status = "Out";
            else if(status.equals("Out"))
                status = "In";
            else
                Toast.makeText(ScanActivity.this, "ERROR: Didnt get desired Status. ",
                        Toast.LENGTH_SHORT).show();
        }else {
            status = "In";
//            isEntryDoc_exists_already = false;      // new doc has to be created each day.
        }
        Location location = new Location(ScanActivity.this);
        Map<String, Object> entry = new HashMap<>();
        entry.put("Status", status);
        entry.put("DateTime", FieldValue.serverTimestamp());
        entry.put("Gate", 1);   //TODO: make it dynamic.
        entry.put("Location", new GeoPoint(location.getLatitude(),location.getLongitude()));   // TODO: make it dynamic.

        db.collection(path + "/Entry/Entry_Doc/" + DateFormat.format("dd-MM-yyyy",new java.util.Date()))
                .document(String.valueOf(DateFormat.format("HH:mm:ss",new java.util.Date())))
                .set(entry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ScanActivity.this, "Entry Registered",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this, "ERROR" +e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                    }
                });

/*

        Temp temp = addEntry_Analysis(entry_status);

        db.document("/DV/Institutions/SGS/Analysis2")
                .set(temp, SetOptions.merge())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this, "ERROR" +e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                    }
                });
*/


        return null;
    }

    public void getStatus(final String path)
    {
        /*final String[] entry_status = {""};
        db.collection(path + "/Entry/Entry_Doc/" + DateFormat.format("dd-MM-yyyy",new java.util.Date()))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()) {
                            entry_status[0] = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.getDocuments().size() - 2).getString("Status");
                            Toast.makeText(ScanActivity.this, "Status: " + entry_status[0],
                                    Toast.LENGTH_SHORT).show();
                            if(entry_status[0].equals("In"))
                                entry_status[0] = "Out";
                            else if(entry_status[0].equals("Out"))
                                entry_status[0] = "In";
                            else
                                Toast.makeText(ScanActivity.this, "ERROR: Didnt get desired Status. ",
                                        Toast.LENGTH_SHORT).show();
                        }else
                            entry_status[0] = "In";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this, "ERROR getting Status: " + e,
                                Toast.LENGTH_SHORT).show();
                        entry_status[0] = "" + e;     //TODO: check it.
                    }
                });
        return entry_status[0];*/

        Task<QuerySnapshot> getStatus = db.collection(path + "/Entry/Entry_Doc/" + DateFormat.format("dd-MM-yyyy",new java.util.Date()))
                .get();
        getStatus.continueWithTask(new Continuation<QuerySnapshot, Task<String>>()
        {
            @Override
            public Task<String> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                QuerySnapshot querySnapshot = task.getResult();
                return insertEntry(path, querySnapshot);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this, "ERROR getting Status: " + e,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void add_Today_To_Week(Today today, final String week_of_month, final String day)
    {
        db.document(Institution_Path + "/Analysis/Statistics/Week/" + week_of_month +"/" + day)
                .set(today)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ScanActivity.this, "Today shifted to "+week_of_month+" "+day+" successfully.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this, "ERROR" +e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                    }
                });
    }

    private void AddEntry() {
        db.document(Institution_Path + "/Analysis/Statistics/Today")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if(!documentSnapshot.getData().isEmpty())
                        {
                            Today today = documentSnapshot.toObject(Today.class);

                            if(getZeroTimeDate(today.getDate()).before(getZeroTimeDate(new Date())))
                            {
                                //TODO: Its a new Day.
                                //overwrite current scanned id's details.
                                Gate_map gate_map = new Gate_map("Gate 1", "Gate 1"); //TODO: make gate dynamic.
                                Time_map time_map = new Time_map(new Date(), getZeroTimeDate(new Date()));

                                Entry_Model entry_model = new Entry_Model("In", gate_map, time_map);

                                List<Entry_Model> entryModelList = new ArrayList<>();
                                entryModelList.add(entry_model);
                                Details details = new Details(entryModelList);

                                Entries_Model entries_model = new Entries_Model(Long.parseLong("1"),Long.parseLong(scannedId), details);

                                List<Entries_Model> entries = new ArrayList<>();
                                entries.add(entries_model);

                                Today newtoday = new Today(new Date(),entries);
                                addToday(newtoday);
                            }
                            else if(getZeroTimeDate(today.getDate()).equals(getZeroTimeDate(new Date())))     //same date.
                            {
                                //TODO: Same Day.
                                for(Entries_Model entries: today.getEntries())
                                {
                                    if(entries.getId() == Long.parseLong(scannedId))        // id entry does exists.
                                    {
                                        //add an entry and update count.
                                        int current_index = today.getEntries().indexOf(entries);    //In below.
                                        if(today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() - 1).getStatus().equals("Out")) {
                                            today.getEntries().get(current_index).setCount(today.getEntries().get(current_index).getCount() + 1);

                                            //TODO: add new In entry to details.
                                            Gate_map gate_map = new Gate_map("Gate 1", "Gate 1");    //TODO: make gate dynamic.
                                            Time_map time_map = new Time_map(new Date(), getZeroTimeDate(new Date()));

                                            Entry_Model entry_model = new Entry_Model("In", gate_map, time_map);
                                            today.getEntries().get(current_index).getDetails().getEntry().add(entry_model);
                                            break;
                                        }   //Out below.
                                        else if(today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() -1).getStatus().equals("In")) {
                                            today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() -1).setStatus("Out");
                                            today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() -1).getGate().setExit_Gate("Gate 1"); //TODO: make gate dynamic.
                                            today.getEntries().get(current_index).getDetails().getEntry().get(today.getEntries().get(current_index).getDetails().getEntry().size() -1).getTime().setExit_Time(new Date());
                                            break;
                                        }// id entry does not exists.
                                    }else if((entries.getId()!= Long.parseLong(scannedId))) {

                                        if (today.getEntries().indexOf(entries) == (today.getEntries().size() - 1)) {
                                            Gate_map gate_map = new Gate_map("Gate 1", "Gate 1");    //TODO: make gate dynamic.
                                            Time_map time_map = new Time_map(new Date(), getZeroTimeDate(new Date()));

                                            Entry_Model entry_model = new Entry_Model("In", gate_map, time_map);

                                            List<Entry_Model> entryModelList = new ArrayList<>();
                                            entryModelList.add(entry_model);
                                            Details details = new Details(entryModelList);

                                            Entries_Model entries_model = new Entries_Model(Long.parseLong("1"), Long.parseLong(scannedId), details);
                                            today.getEntries().add(entries_model);
                                        }
                                    }
                                }
                                //TODO: overwrite today with current data.
                                addToday(today);
                            }
                        }else{
                            //TODO: add current scanned id details.
                            Gate_map gate_map = new Gate_map("Gate 1", "Gate 1"); //TODO: make gate dynamic.
                            Time_map time_map = new Time_map(new Date(), getZeroTimeDate(new Date()));

                            Entry_Model entry_model = new Entry_Model("In", gate_map, time_map);

                            List<Entry_Model> entryModelList = new ArrayList<>();
                            entryModelList.add(entry_model);
                            Details details = new Details(entryModelList);

                            Entries_Model entries_model = new Entries_Model(Long.parseLong("1"),Long.parseLong(scannedId), details);

                            List<Entries_Model> entries = new ArrayList<>();
                            entries.add(entries_model);

                            Today today = new Today(new Date(),entries);
                            Map<String, Today> today_hashMap = new HashMap<>();
                            today_hashMap.put("Today",today);
                            addToday(today);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this, "ERROR" +e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                    }
                });
    }

    public void addToday(Today today)
    {
        db.document(Institution_Path + firestore_analysis_to_Year )
                .set(today)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ScanActivity.this, "Today updated successfully.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanActivity.this, "ERROR" +e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                    }
                });
    }

    public String getWeek(int week_number)
    {
        String week = null;
        switch(week_number)
        {
            case 1: {    week = "First_Week";
                break;
            }
            case 2: {    week = "Second_Week";
                break;
            }case 3: {   week = "Third_Week";
            break;
        }case 4: {   week = "Fourth_Week";
            break;
        }case 5: {   week = "Fifth_Week";
            break;
        }
        }
        return week;
    }
}
