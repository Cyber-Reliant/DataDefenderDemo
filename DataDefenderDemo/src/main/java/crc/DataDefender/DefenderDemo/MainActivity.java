package crc.DataDefender.DefenderDemo;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import CRC.DataDefender.DefenderDemo.R;
import CRC.DataProtect.CRCFileIO.CRCFile;
import CRC.DataProtect.CRCFileIO.CRCFileReader;
import CRC.DataProtect.CRCFileIO.CRCInputStream;
import CRC.DataProtect.CRCFileIO.CRCOutputStream;
import CRC.DataProtect.CRCFileIO.CRCReadWrite;
import CRC.DataProtect.database.SQLiteDatabase;
import CRC.DataProtect.database.SQLiteStatement;
import CRC.MobileDataDefender.CRCDataDefender;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import crc.DataDefender.DefenderDemo.Room.RoomTest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView myTV;          /* Text view widget */
    private int myNTest;            /* Number of tests attempted */
    private int myNErr;             /* Number of tests failed */
    private static boolean isinit = false;
    File DB_PATH;
    private CRCDataDefender provider;
    private ScreenReceiver mReceiver;
    Handler mHandler;

    enum TestHarness{
        DEBUG, //use this call if not wanting ank key management at all. KEK is 'hard coded'
        MS, //use this call for ManagementService integrated KEK and authentication management
        DD; //use this call to demo App managed KEK creation and authentication
    }
    enum SDKPath{// does not affect DB I/O
        DEFENDER,// uses the DefenderObject calls for all file IO
        DIRECT,//Bypass the Defender calls and use the CRCFile and CRCStreaming classes directly
        BASE ;//Use the direct calls into the base of the streaming classes for most direct File I/O
    }
    private SDKPath IOPath = SDKPath.DEFENDER;//change this to Use alternate paths into the SDK for File I/O
    private TestHarness USE = TestHarness.DD;//change this for alternate paths into Authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTV = (TextView)findViewById(R.id.tv_widget);

        Context context = getApplicationContext();
        mReceiver = new ScreenReceiver(context);
        Application application = (Application)context;
    }

    boolean changed = false;

    @Override
    protected void onPause() {
        myTV.append("OnPause\n\n");
        super.onPause();
        //remove the KEK from the protection system
        CRCDataDefender.deAuthDefender();
        myTV.append("\n De-Authenticated\n");
        if(!changed) {
            if(USE == TestHarness.DD)
            {//just a demomstration happens only once
                CRCDataDefender.resetPassword(getApplicationContext(), "7777777", "88888888");
                myTV.append("\n Password Changed from 7777777 to 88888888\n");
            }
            changed = true;
        }
        myTV.append("\n Password Changed\n");
    }

    @Override
    protected void onDestroy() {
        myTV.append("OnDestroy\n\n");
        super.onDestroy();

    }
    @Override
    protected void onResume() {
        super.onResume();
        //this recieves notification from DataDefender of authentication status
        initBroadcastReciever(getApplicationContext());
        // this swithches between authentication modes for testing
        //set USE
        switch(USE)
        {
            case DEBUG:
                //use this call if not wanting ank key management at all. KEK is 'hard coded'
                CRCDataDefender.initDefenderDebug(getApplicationContext(),2,2);
                break;
            case DD:
                //use this call to demo App managed KEK creation and authentication
                //if it fails, that means that a password is already set, so falls through to authentication.
                if(CRCDataDefender.initWithPassword(getApplicationContext(), "7777777", 2, 2))
                {
                    //using demo password for key creation and storage "7777777"
                    //if Key is created and stored properly, Authenticate with the same password which
                    //initializes the protection system
                    CRCDataDefender.authenticateDefender(getApplicationContext(),"7777777");
                    myTV.append("\nCreated Password and Authenticated 7777777\n\n");
                }else{
                    //this is after a password change in the 'OnPause call so authenticating with a new password "88888888"
                    if(CRCDataDefender.authenticateDefender(getApplicationContext(),"88888888"))
                    {
                        myTV.append("\nAuthenticated with password 88888888\n\n");
                        changed = true;
                    }else{
                        //this is with no password change in the 'OnPause call so authenticating with a new password "7777777"
                        CRCDataDefender.authenticateDefender(getApplicationContext(),"7777777");
                        myTV.append("\nAuthenticated with 7777777\n\n");
                    }

                }
                break;
            case MS:
                //use this call for ManagementService integrated KEK and authentication management
                CRCDataDefender.initDataDefender(getApplicationContext());
                break;
            default:
                break;
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        myTV.append("OnStart\n\n");
        Log.e("App test","onActivityStarted");
    }

    @Override
    protected void onStop() {
        myTV.append("OnStop\n\n");
        super.onStop();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        myTV.append("OnRestart\n\n");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private  void initBroadcastReciever(Context context)
    {
        LocalBroadcastManager mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter filter = new IntentFilter();

        filter.addAction("CRC.CRCDataDefender.Auth");

        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("CRC.CRCDataDefender.Auth")) {

                    isinit = intent.getBooleanExtra("status", true);
                    if(isinit)
                        myTV.append("\nAuthenticated\n\n");
                }

            }
        };
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    public void run_the_tests(View view){
        myTV.setText("");
        view.post(new Runnable() {
            @Override
            public void run() {
                run_tests();
            }
        });
    }

    public void run_tests(){

        DB_PATH = getApplicationContext().getDatabasePath("test.db");

        myTV.setText("");
        myNErr = 0;
        myNTest = 0;

        try {
            //this initializes for SDK for testing
            report_version();
            //This steps through several File I/O demos
            FileIO_test();
            //This steps through simple Db I/O demos
            DB_test();

            Room_test();

            myTV.append("\n" + myNErr + " errors from " + myNTest + " tests\n");
        } catch(Exception e) {
            myTV.append("Exception: " + e.toString() + "\n");
            myTV.append(android.util.Log.getStackTraceString(e) + "\n");
        }
    }

    public void report_version(){
        SQLiteDatabase db = null;
        SQLiteStatement st;
        String res;
        //here we create the CRC SDK object
        provider = new CRCDataDefender();
        //since we are using a memory call, we have to use the direct SQLite call using
        //the CRC.DataProtect.database.SQLiteDatabase which is also called from within the SDK
        db = SQLiteDatabase.openOrCreateDatabase(":memory:", null);
        st = db.compileStatement("SELECT sqlite_version()");
        res = st.simpleQueryForString();
        myTV.append("SQLite version " + res + "\n\n");
        db.close();

    }

    public void test_warning(String name, String warning){
        myTV.append("WARNING:" + name + ": " + warning + "\n");
    }

    public void test_result(String name, String res, String expected, long t0){
        long tot = (System.nanoTime() - t0) / 1000000;

        mHandler.post(() -> {
            myTV.append(name + "... ");
            myNTest++;

            if( res.equals(expected) ){
                myTV.append("ok (" + tot + "ms)\n");
            } else {
                myNErr++;
                myTV.append("FAILED\n");
                myTV.append("   res=     \"" + res + "\"\n");
                myTV.append("   expected=\"" + expected + "\"\n");
            }
        });
    }

    public void Room_test() {
        final String roomTag = TAG + "/Room";
        Log.d(roomTag, "room test begin");
        AsyncTask.execute(() -> {
            final long t0 = System.nanoTime();

            Log.d(roomTag, "removing any previous app.db");
            SQLiteDatabase.deleteDatabase(getDatabasePath("app.db"));

            RoomTest.Companion.test_single_insert(getApplicationContext());
            test_result("Room_test_single_insert", "", "", t0);

            final long t1 = System.nanoTime();
            RoomTest.Companion.test_multi_insert(getApplicationContext());
            test_result("Room_test_multi_insert", "", "", t1);

            final long t2 = System.nanoTime();
            RoomTest.Companion.test_get_all(getApplicationContext());
            test_result("Room_test_get_all", "", "", t2);

            final long t3 = System.nanoTime();
            RoomTest.Companion.test_get_by_id(getApplicationContext());
            test_result("Room_test_get_by_id", "", "", t3);

            final long t4 = System.nanoTime();
            RoomTest.Companion.test_delete_all(getApplicationContext());
            test_result("Room_test_delete_all", "", "", t4);
        });

    }

    /*
     ** Use a Cursor to loop through the results of a SELECT query.
     */
    public void DB_test() throws Exception {
        final long t0 = System.nanoTime();
        SQLiteDatabase.deleteDatabase(DB_PATH);
        SQLiteDatabase db = null;
        //these all result in a call to CRC.DataProtect.database.SQLiteDatabase
        switch(IOPath)
        {
            case DEFENDER:
                db =  provider.createDatabase(DB_PATH);
                break;
            case DIRECT:
                db = SQLiteDatabase.openOrCreateDatabase(DB_PATH.getPath(),null);
                break;
            default:
                db =  provider.createDatabase(DB_PATH);
                break;
        }

        String res = "";
        String expect = "";
        int i;
        int nRow = 0;

        db.execSQL("CREATE TABLE t1(x)");
        db.execSQL("BEGIN");
        for(i=0; i<1000; i++){
            db.execSQL("INSERT INTO t1 VALUES ('one'), ('two'), ('three')");
            expect += ".one.two.three";
        }
        db.execSQL("COMMIT");
        Cursor c = db.rawQuery("SELECT x FROM t1", null);
        if( c!=null ){
            boolean bRes;
            for(bRes=c.moveToFirst(); bRes; bRes=c.moveToNext()){
                String x = c.getString(0);
                res = res + "." + x;
            }
        }else{
            test_warning("csr_test_1", "c==NULL");
        }
        test_result("Commit_test", res, expect, t0);

        final long t1 = System.nanoTime();
        db.execSQL("BEGIN");
        for(i=0; i<1000; i++){
            db.execSQL("INSERT INTO t1 VALUES (X'123456'), (X'789ABC'), (X'DEF012')");
            db.execSQL("INSERT INTO t1 VALUES (45), (46), (47)");
            db.execSQL("INSERT INTO t1 VALUES (8.1), (8.2), (8.3)");
            db.execSQL("INSERT INTO t1 VALUES (NULL), (NULL), (NULL)");
        }
        db.execSQL("COMMIT");

        c = db.rawQuery("SELECT x FROM t1", null);
        if( c!=null ){
            boolean bRes;
            for(bRes=c.moveToFirst(); bRes; bRes=c.moveToNext()) nRow++;
        }else{
            test_warning("csr_test_1", "c==NULL");
        }
        test_result("SELECT_test", "" + nRow, "15000", t1);

        db.close();
    }
//use this to substitute testing of other functionality
    public void FileIO_test1() throws Exception {
        switch(IOPath)
        {
            case DEFENDER:
                DefenderIO_test1();
                break;
            case DIRECT:
                DirectIO_test1();
                break;
            case BASE:
                BaseIO_test1();
                break;
        }
    }
    public void DefenderIO_test1() throws Exception {
        final long t0 = System.nanoTime();
        int length = 1024;

        byte[] buffer = new byte[length];
        byte[] inbuffer = new byte[length];
        java.util.Arrays.fill(buffer,(byte)5);

        CRCReadWrite.SetLogging(true);

        int read = 128;

        test_result("FileIO_test", "" + read, "128", t0);
    }
    public void DirectIO_test1() throws Exception {
        final long t0 = System.nanoTime();
        int length = 1024;

        byte[] buffer = new byte[length];
        byte[] inbuffer = new byte[length];
        java.util.Arrays.fill(buffer,(byte)5);

        CRCReadWrite.SetLogging(true);

        int read = 128;

        test_result("FileIO_test", "" + read, "128", t0);
    }

    public void BaseIO_test1() throws Exception {
        final long t0 = System.nanoTime();
        int length = 1024;

        byte[] buffer = new byte[length];
        byte[] inbuffer = new byte[length];
        java.util.Arrays.fill(buffer,(byte)5);


        CRCReadWrite.SetLogging(true);

        int read = 128;

        test_result("FileIO_test", "" + read, "128", t0);
    }



    public void FileIO_test() throws Exception {
        switch (IOPath) {
            case DEFENDER:
                DefenderIO_test();
                break;
            case DIRECT:
                DirectIO_test();
                break;
            case BASE:
                BaseIO_test();
                break;
        }
    }

    public void DefenderIO_test() throws Exception {
        final long t0 = System.nanoTime();
        byte[] buffer = new byte[1024];
        byte[] inbuffer = new byte[1024];
        char[] cinbuffer = new char[1024];
        java.util.Arrays.fill(buffer,(byte)5);

        int length = 1024;

        CRCReadWrite.SetLogging(true);
        File AB_PATH = getApplicationContext().getDatabasePath("RenameTest.txt");
        File XB_PATH = getApplicationContext().getDatabasePath("FileIOtest.txt");
        int len = XB_PATH.getPath().length();
        FileOutputStream outstream = provider.getOutputStream(XB_PATH,true);
        outstream.close();
        String[] databaseList =  getApplicationContext().databaseList();


        FileReader rinstream = provider.getFileReader(XB_PATH);
        databaseList =  getApplicationContext().databaseList();
        outstream = provider.getOutputStream(XB_PATH,true);
        int read = 0;
        String out = "NONE";
        try {
            read = rinstream.read(cinbuffer, 0, inbuffer.length);
            outstream.write (inbuffer, 0, inbuffer.length);
            outstream.write (inbuffer, 0, inbuffer.length);

            read = rinstream.read(cinbuffer, 0, inbuffer.length);
            read = rinstream.read(cinbuffer, 0, inbuffer.length);
            outstream.write (inbuffer, 0, inbuffer.length);
            outstream.close();
            read = rinstream.read(cinbuffer, 0, inbuffer.length);
            rinstream.close();
        }catch(Exception e){
            out = e.getMessage();
        }
        test_result("FileIO Read_Write_sync_test", "" + read, "1024", t0);

        //this demonstrates renaming (encrypted to encrypted)
        final long t1 = System.nanoTime();
        databaseList =  getApplicationContext().databaseList();
        boolean ok = provider.renameTo(XB_PATH,AB_PATH);
        databaseList =  getApplicationContext().databaseList();
        File testB_PATH = new File(XB_PATH.getParent());
        databaseList =  provider.list(testB_PATH);
        FileInputStream instream = provider.getInputStream(AB_PATH);
        read = instream.read(inbuffer, 0, inbuffer.length);
        test_result("FileIO rename_test", "" + read, "1024", t1);

        // this demondstrates Channel use
        final long t2 = System.nanoTime();
        FileChannel Channel = instream.getChannel();
        long size = Channel.size();
        ByteBuffer inbuff = ByteBuffer.allocate(length);
        ByteBuffer outbuff = ByteBuffer.allocate(length);
        outbuff.put(inbuffer,0,1024);
        int offset = 128;
        outbuff.position(0);
        outbuff.limit(offset);
        inbuff.position(0);
        inbuff.limit(offset);
        read = 0;
        int written = 0;
        boolean skip = false;
        inbuff.limit(read + offset);
        read += Channel.read(inbuff);
        long pos =Channel.position();
        instream.close();
        Channel.close();
        test_result("FileIO Channel_test", "" + read, "128", t2);
        //here we decrypt the renamed encrypted file
        final long r2 = System.nanoTime();
        File UN_PATH = getApplicationContext().getDatabasePath("RawRenameTest.txt");
        ok = provider.decryptTo(AB_PATH,UN_PATH);
        FileReader readstream = new FileReader(UN_PATH);
        read = readstream.read(cinbuffer, 0, inbuffer.length);
        readstream.close();
        test_result("FileIO decrypt test", "" + read, "1024", r2);

        //now the final is a pure encrypt test
      final long x2 = System.nanoTime();
        File RE_PATH = getApplicationContext().getDatabasePath("EncryptRawRenameTest.txt");
        ok = provider.encryptTo(UN_PATH,RE_PATH );
        instream = provider.getInputStream(RE_PATH);
        read = instream.read(inbuffer, 0, inbuffer.length);
        readstream.close();
        test_result("FileIO encrypt test", "" + read, "1024", x2);
    }

    public void DirectIO_test() throws Exception {
        final long t0 = System.nanoTime();
        byte[] buffer = new byte[1024];
        byte[] inbuffer = new byte[1024];
        char[] cinbuffer = new char[1024];
        java.util.Arrays.fill(buffer,(byte)5);

        int length = 1024;

        CRCReadWrite.SetLogging(true);
        File AB_PATH = getApplicationContext().getDatabasePath("RenameTest.txt");
        File XB_PATH = getApplicationContext().getDatabasePath("FileIOtest.txt");
        int len = XB_PATH.getPath().length();
        FileOutputStream outstream = new CRCOutputStream(XB_PATH,true);
        outstream.close();
        String[] databaseList =  getApplicationContext().databaseList();


        FileReader rinstream = new CRCFileReader(XB_PATH);
        databaseList =  getApplicationContext().databaseList();
        outstream = new CRCOutputStream(XB_PATH,true);
        int read = 0;
        String out = "NONE";
        try {
            read = rinstream.read(cinbuffer, 0, inbuffer.length);
            outstream.write (inbuffer, 0, inbuffer.length);
            outstream.write (inbuffer, 0, inbuffer.length);

            read = rinstream.read(cinbuffer, 0, inbuffer.length);
            read = rinstream.read(cinbuffer, 0, inbuffer.length);
            outstream.write (inbuffer, 0, inbuffer.length);
            outstream.close();
            read = rinstream.read(cinbuffer, 0, inbuffer.length);
            rinstream.close();
        }catch(Exception e){
            out = e.getMessage();
        }
        test_result("FileIO Read_Write_sync_test", "" + read, "1024", t0);

        //this demonstrates renaming (encrypted to encrypted)
        final long t1 = System.nanoTime();
        databaseList =  getApplicationContext().databaseList();
        boolean ok = new CRCFile(XB_PATH.getPath()).renameTo(AB_PATH);
        databaseList =  getApplicationContext().databaseList();
        File testB_PATH = new File(XB_PATH.getParent());
        databaseList =  new File(testB_PATH.getPath()).list();//does not filter out shred directories
        FileInputStream instream = new CRCInputStream(AB_PATH);
        read = instream.read(inbuffer, 0, inbuffer.length);
        test_result("FileIO rename_test", "" + read, "1024", t1);


        // this demondstrates Channel use
        final long t2 = System.nanoTime();
        FileChannel Channel = instream.getChannel();
        long size = Channel.size();
        ByteBuffer inbuff = ByteBuffer.allocate(length);
        ByteBuffer outbuff = ByteBuffer.allocate(length);
        outbuff.put(inbuffer,0,1024);
        int offset = 128;
        outbuff.position(0);
        outbuff.limit(offset);
        inbuff.position(0);
        inbuff.limit(offset);
        read = 0;
        int written = 0;
        boolean skip = false;
        inbuff.limit(read + offset);
        read += Channel.read(inbuff);
        long pos =Channel.position();
        instream.close();
        Channel.close();
        test_result("FileIO Channel_test", "" + read, "128", t2);

        //here we decrypt the renamed encrypted file
        final long r2 = System.nanoTime();
        File UN_PATH = getApplicationContext().getDatabasePath("RawRenameTest.txt");
        ok = new CRCFile(AB_PATH.getPath()).decryptTo(UN_PATH);

        FileReader readstream = new FileReader(UN_PATH);
        read = readstream.read(cinbuffer, 0, inbuffer.length);
        readstream.close();
        test_result("FileIO decrypt test", "" + read, "1024", r2);

        //now the final is a pure encrypt test
        final long x2 = System.nanoTime();
        File RE_PATH = getApplicationContext().getDatabasePath("EncryptRawRenameTest.txt");
        ok = new CRCFile(UN_PATH.getPath()).encryptTo(RE_PATH );
        instream = new CRCInputStream(RE_PATH);
        read = instream.read(inbuffer, 0, inbuffer.length);
        readstream.close();
        test_result("FileIO encrypt test", "" + read, "1024", x2);
    }

    public void BaseIO_test() throws Exception {
        final long t0 = System.nanoTime();
        byte[] buffer = new byte[1024];
        byte[] inbuffer = new byte[1024];
        char[] cinbuffer = new char[1024];
        java.util.Arrays.fill(buffer,(byte)5);

        int length = 1024;

        CRCReadWrite.SetLogging(true);
        //here we create a debug list before file creation
        String[] databaseList =  getApplicationContext().databaseList();

        File AB_PATH = getApplicationContext().getDatabasePath("RenameTest.txt");

        File XB_PATH = getApplicationContext().getDatabasePath("FileIOtest.txt");
        CRCFile XBFile = new CRCFile(XB_PATH.getAbsolutePath());

        CRCReadWrite writeInterface  = new CRCReadWrite();
        writeInterface.writeOpen(XBFile.getAbsolutePath(), XBFile.isEncrypted(),true,false);
        CRCReadWrite readInterface  = new CRCReadWrite();
        readInterface.readOpen(XBFile.getAbsolutePath(), XBFile.isEncrypted());
       //here we collect the same list to compare
        databaseList =  getApplicationContext().databaseList();

        int read = 0;
        String out = "NONE";
        //this demonstrates reading and writing of the same file once opened
        //shows file syncing as well as standard I/O calls
        try {
            read = readInterface.read(buffer, 0, inbuffer.length);
            writeInterface.write (inbuffer, 0, inbuffer.length);
            writeInterface.write (inbuffer, 0, inbuffer.length);

            read = readInterface.read(buffer, 0, inbuffer.length);
            read = readInterface.read(buffer, 0, inbuffer.length);
            writeInterface.write (inbuffer, 0, inbuffer.length);
            writeInterface.Close();
            read = readInterface.read(buffer, 0, inbuffer.length);
            readInterface.Close();
        }catch(Exception e){
            out = e.getMessage();
        }
        test_result("FileIO Read_Write_sync_test", "" + read, "1024", t0);

        //this demonstrates renaming (encrypted to encrypted)
        final long t1 = System.nanoTime();
        databaseList =  getApplicationContext().databaseList();
        boolean ok = XBFile.renameTo(AB_PATH);
        databaseList =  getApplicationContext().databaseList();
        CRCFile testB_PATH = new CRCFile(XB_PATH.getParent());
        databaseList =  provider.list(testB_PATH);
        CRCFile ABFile = new CRCFile(AB_PATH.getAbsolutePath());
        readInterface  = new CRCReadWrite();
        readInterface.readOpen(ABFile.getAbsolutePath(), ABFile.isEncrypted());
        read = readInterface.read(inbuffer, 0, inbuffer.length);
        readInterface.Close();
        test_result("FileIO rename_test", "" + read, "1024", t1);

        //here we decrypt the renamed encrypted file
        final long r2 = System.nanoTime();
        File UN_PATH = getApplicationContext().getDatabasePath("RawRenameTest.txt");
        ok = ABFile.decryptTo(UN_PATH);
        //read the unencrypted data with Java IO reader
        FileReader readstream = new FileReader(UN_PATH);
        read = readstream.read(cinbuffer, 0, inbuffer.length);
        readstream.close();
        test_result("FileIO decrypt test", "" + read, "1024", r2);

        //now the final is a pure encrypt test
        final long x2 = System.nanoTime();
        File RE_PATH = getApplicationContext().getDatabasePath("EncryptRawRenameTest.txt");
        CRCFile UNFile = new CRCFile(UN_PATH.getAbsolutePath());
        ok = UNFile.encryptTo(RE_PATH );
        //now we try to read the newly encrypted file
        CRCFile REFile = new CRCFile(RE_PATH.getAbsolutePath());
        readInterface  = new CRCReadWrite();
        readInterface.readOpen(REFile.getAbsolutePath(), REFile.isEncrypted());
        read = readInterface.read(inbuffer, 0, inbuffer.length);
        readstream.close();
        test_result("FileIO encrypt test", "" + read, "1024", x2);
    }


}
