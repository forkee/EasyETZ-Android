
package com.etzwallet.presenter.activities.intro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.etzwallet.R;
import com.etzwallet.presenter.activities.HomeActivity;
import com.etzwallet.presenter.activities.SetPinActivity;
import com.etzwallet.presenter.activities.util.BRActivity;
import com.etzwallet.tools.animation.BRAnimator;
import com.etzwallet.tools.security.BRKeyStore;
import com.etzwallet.tools.security.PostAuth;
import com.etzwallet.tools.security.SmartValidator;
import com.etzwallet.tools.threads.executor.BRExecutor;
import com.etzwallet.tools.util.Utils;
import com.etzwallet.wallet.WalletsMaster;
import com.platform.APIClient;

import java.io.Serializable;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 8/4/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class IntroActivity extends BRActivity implements Serializable {
    private static final String TAG = IntroActivity.class.getName();
    public Button newWalletButton;
    public Button recoverWalletButton;
    public static boolean appVisible = false;
    private static IntroActivity app;
    private View splashScreen;

    public static IntroActivity getApp() {
        return app;
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        newWalletButton = findViewById(R.id.button_new_wallet);
        recoverWalletButton = findViewById(R.id.button_recover_wallet);
        splashScreen = findViewById(R.id.splash_screen);
        setListeners();
        updateBundles();
//        ImageButton faq = findViewById(R.id.faq_button);

//        faq.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!BRAnimator.isClickAllowed()) return;
//                BaseWalletManager wm = WalletsMaster.getInstance(IntroActivity.this).getCurrentWallet(IntroActivity.this);
//                BRAnimator.showSupportFragment(IntroActivity.this, BRConstants.FAQ_START_VIEW, wm);
//            }
//        });

        getWindowManager().getDefaultDisplay().getSize(screenParametersPoint);

        if (Utils.isEmulatorOrDebug(this)) {
            Utils.printPhoneSpecs();
        }

        byte[] masterPubKey = BRKeyStore.getMasterPublicKey(this);

        boolean isFirstAddressCorrect = false;
        if (masterPubKey != null && masterPubKey.length != 0) {
            isFirstAddressCorrect = SmartValidator.checkFirstAddress(this, masterPubKey);
        }
        if (!isFirstAddressCorrect) {
            WalletsMaster.getInstance(this).wipeWalletButKeystore(this);
        }

        PostAuth.getInstance().onCanaryCheck(this, false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashScreen.setVisibility(View.GONE);
            }
        }, DateUtils.SECOND_IN_MILLIS);





    }



    private void updateBundles() {
        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                final long startTime = System.currentTimeMillis();
                APIClient apiClient = APIClient.getInstance(IntroActivity.this);
                apiClient.updateBundle();
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "updateBundle DONE in " + (endTime - startTime) + "ms");
            }
        });
    }

    private void setListeners() {
        newWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                HomeActivity bApp = HomeActivity.getApp();
                Intent intent = new Intent(IntroActivity.this, SetPinActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                if (bApp != null) bApp.finish();
            }
        });

        recoverWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                HomeActivity bApp = HomeActivity.getApp();
                if (bApp != null) bApp.finish();
                Intent intent = new Intent(IntroActivity.this, RecoverActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                if (bApp != null) bApp.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
