package tud.seemuh.nfcgate.gui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.fragments.RelayFragment;
import tud.seemuh.nfcgate.gui.tabLayout.SlidingTabLayout;
import tud.seemuh.nfcgate.gui.tabLogic.PagerAdapter;
import tud.seemuh.nfcgate.nfc.hce.DaemonConfiguration;

public class MainActivity extends FragmentActivity
        implements ReaderCallback {
        //implements token_dialog.NoticeDialogListener, enablenfc_dialog.NFCNoticeDialogListener, ReaderCallback{

    private NfcAdapter mAdapter;
    private IntentFilter mIntentFilter = new IntentFilter();
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    private final static String TAG = "MainActivity";

    //Connection Client
    //protected HighLevelNetworkHandler mConnectionClient;

    // NFC Manager
    //public NfcManager mNfcManager;

    // Defined name of the Shared Preferences Buffer
    //TODO this is now DOUBLE DEFINED: here and in the RelayFragment
    public static final String PREF_FILE_NAME = "SeeMoo.NFCGate.Prefs";



    //private Callback mNetCallback = new ProtobufCallback();

    // declares main functionality
    //private Button mReset, mConnecttoSession, mAbort, mJoinSession;
    //private TextView mConnStatus, mInfo, mDebuginfo, mIP, mPort, mPartnerDevice, mtoken;

    //FIXME double in RelayFragment
    public static String joinSessionMessage = "Join Session";
    public static String createSessionMessage = "Create Session";
    public static String leaveSessionMessage = "Leave Session";
    public static String resetMessage = "Reset";
    public static String resetCardMessage = "Forget Card";

    private SlidingTabLayout mSlidingTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mIntentFilter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);

        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        //This is for the fancy tabs
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(pager);

        if (!mAdapter.isEnabled()) {
            // NFC is not enabled -> "Tell the user to enable NFC"
            //FIXME NPE here
            //RelayFragment.getInstance().showEnableNFCDialog();
        }

        // Create a generic PendingIntent that will be delivered to this activity.
        // The NFC stack will fill in the intent with the details of the discovered tag before
        // delivering to this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Setup an foreground intent filter for NFC
        IntentFilter tech = new IntentFilter();
        tech.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        mFilters = new IntentFilter[] { tech, };
        // this thing must have the same structure as in the tech.xml
        mTechLists = new String[][] {
                new String[] {NfcA.class.getName()},
                new String[] {Ndef.class.getName()},
                new String[] {IsoDep.class.getName()}
                //we could add all of the Types from the tech.xml here
        };


    }

    @Override
    public void onResume() {
        super.onResume();

        // Load values from the Shared Preferences Buffer
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);

        if (mAdapter != null && mAdapter.isEnabled()) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);

            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent().getAction())) {
                Log.i(TAG, "onResume(): starting onNewIntent()...");
                this.onNewIntent(this.getIntent());
            }
        }

        //ReaderMode
        boolean isReaderModeEnabled = preferences.getBoolean("mReaderModeEnabled", false);
        if(isReaderModeEnabled) {
            //This cast to ReaderCallback seems unavoidable, stupid Java...
            mAdapter.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        } else {
            mAdapter.disableReaderMode(this);
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.i(TAG, "onResume(): intent: " + getIntent().getAction());
//
//        // Load values from the Shared Preferences Buffer
//        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
//
//        if (mAdapter != null && mAdapter.isEnabled()) {
//            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
//
//            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
//                Log.i(TAG, "onResume(): starting onNewIntent()...");
//                onNewIntent(getIntent());
//            }
//        }
//
//        ip = preferences.getString("ip", "192.168.178.31");
//        port = preferences.getInt("port", 5566);
//        globalPort = preferences.getInt("port", 5566);
//
//        //on start set the text values
//        if(mIP.getText().toString().trim().length() == 0) {
//            mIP.setText(ip);
//            mPort.setText(String.valueOf(port));
//        }
//
//        boolean chgsett;
//        chgsett = preferences.getBoolean("changed_settings", false);
//
//        if(chgsett) {
//            mIP.setText(ip);
//            mPort.setText(String.valueOf(port));
//
//            // reset the 'settings changed' flag
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putBoolean("changed_settings", false);
//            editor.commit();
//        }
//
//        //ReaderMode
//        boolean isReaderModeEnabled = preferences.getBoolean("mReaderModeEnabled", false);
//        if(isReaderModeEnabled) {
//            mAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
//        } else {
//            mAdapter.disableReaderMode(this);
//        }
//
//        // De- or Enables Debug Window
//        mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
//        mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
//        if (mDevModeEnabled) {
//            mDebuginfo.setVisibility(View.VISIBLE);
//            mDebuginfo.requestFocus();
//        } else {
//            mDebuginfo.setVisibility(View.GONE);  // View.invisible results in an error
//        }
//
//        mConnecttoSession.requestFocus();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//
//        mAdapter.disableForegroundDispatch(this);
//    }
//

    private void onTagDiscoveredCommon(Tag tag) {
        // Pass reference to NFC Manager
        //mNfcManager.setTag(tag);
        Log.d(TAG, "onTagDiscoveredCommon");

        //RelayFragment fragment = (RelayFragment) getSupportFragmentManager().getFragments().get(0);
        //fragment.mNfcManager.setTag(tag);
        RelayFragment.getInstance().mNfcManager.setTag(tag);
    }

    /**
     * Function to get tag when readerMode is enabled
     * @param tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {

        Log.i(TAG, "Discovered tag in ReaderMode");
        onTagDiscoveredCommon(tag);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent(): started");
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Log.i(TAG,"Discovered tag with intent: " + intent);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            onTagDiscoveredCommon(tag);

            //This toast is very useful for debugging, DONT delete it!!!
            Toast.makeText(this, "Found Tag", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case  R.id.action_settings:
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), 0);
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
            case R.id.action_getpatchstate:
                Toast.makeText(this, "Patch state: " + (DaemonConfiguration.getInstance().isPatchEnabled() ? "Active" : "Inactive"), Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_disablepatch:
                DaemonConfiguration.getInstance().disablePatch();
                Toast.makeText(this, "Patch disabled", Toast.LENGTH_LONG).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }






}
