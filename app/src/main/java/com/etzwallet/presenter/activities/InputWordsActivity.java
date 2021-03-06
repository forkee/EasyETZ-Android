package com.etzwallet.presenter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.etzwallet.R;
import com.etzwallet.presenter.activities.intro.IntroActivity;
import com.etzwallet.presenter.activities.util.BRActivity;
import com.etzwallet.presenter.customviews.BRDialogView;
import com.etzwallet.tools.animation.BRAnimator;
import com.etzwallet.tools.animation.BRDialog;
import com.etzwallet.tools.animation.SpringAnimator;
import com.etzwallet.tools.manager.BRReportsManager;
import com.etzwallet.tools.manager.BRSharedPrefs;
import com.etzwallet.tools.security.AuthManager;
import com.etzwallet.tools.security.PostAuth;
import com.etzwallet.tools.security.SmartValidator;
import com.etzwallet.tools.util.Utils;
import com.etzwallet.wallet.WalletsMaster;
import com.etzwallet.wallet.wallets.bitcoin.BaseBitcoinWalletManager;

import java.util.ArrayList;
import java.util.List;

public class InputWordsActivity extends BRActivity implements View.OnFocusChangeListener {
    private static final String TAG = InputWordsActivity.class.getName();
    private Button mNextButton;

    private static final int NUMBER_OF_WORDS = 12;
    private static final int LAST_WORD_INDEX = 11;

    public static final String EXTRA_UNLINK = "com.etzwallet.EXTRA_UNLINK";
    public static final String EXTRA_RESET_PIN = "com.etzwallet.EXTRA_RESET_PIN";

    private List<EditText> mEditWords = new ArrayList<>(NUMBER_OF_WORDS);

    private String mDebugPhrase;

    //will be true if this screen was called from the restore screen
    private boolean mIsRestoring = false;
    private boolean mIsResettingPin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_words);

        if (Utils.isEmulatorOrDebug(this)) {
//            mDebugPhrase = "こせき　ぎじにってい　けっこん　せつぞく　うんどう　ふこう　にっすう　こせい　きさま　なまみ　たきび　はかい";//japanese
//            mDebugPhrase = "video tiger report bid suspect taxi mail argue naive layer metal surface";//english
//            mDebugPhrase = "vocation triage capsule marchand onduler tibia illicite entier fureur minorer amateur lubie";//french
//            mDebugPhrase = "zorro turismo mezcla nicho morir chico blanco pájaro alba esencia roer repetir";//spanish
//            mDebugPhrase = "怨 贪 旁 扎 吹 音 决 廷 十 助 畜 怒";//chinese
//            mDebugPhrase = "aim lawn sniff tenant coffee smoke meat hockey glow try also angle";
//            mDebugPhrase = "oído largo pensar grúa vampiro nación tomar agitar mano azote tarea miedo";

        }

        mNextButton = findViewById(R.id.send_button);

        if (Utils.isUsingCustomInputMethod(this)) {
            BRDialog.showCustomDialog(this, getString(R.string.JailbreakWarnings_title), getString(R.string.Alert_customKeyboard_android),
                    getString(R.string.Button_ok), getString(R.string.JailbreakWarnings_close), new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                            imeManager.showInputMethodPicker();
                            brDialogView.dismissWithAnimation();
                        }
                    }, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, 0);
        }

//        ImageButton faq = findViewById(R.id.faq_button);
//
//        faq.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!BRAnimator.isClickAllowed()) return;
//                BaseWalletManager wm = WalletsMaster.getInstance(InputWordsActivity.this).getCurrentWallet(InputWordsActivity.this);
//                BRAnimator.showSupportFragment(InputWordsActivity.this, BRConstants.FAQ_PAPER_KEY, wm);
//            }
//        });

        TextView title = findViewById(R.id.title);
        TextView description = findViewById(R.id.description);

        mEditWords.add((EditText) findViewById(R.id.word1));
        mEditWords.add((EditText) findViewById(R.id.word2));
        mEditWords.add((EditText) findViewById(R.id.word3));
        mEditWords.add((EditText) findViewById(R.id.word4));
        mEditWords.add((EditText) findViewById(R.id.word5));
        mEditWords.add((EditText) findViewById(R.id.word6));
        mEditWords.add((EditText) findViewById(R.id.word7));
        mEditWords.add((EditText) findViewById(R.id.word8));
        mEditWords.add((EditText) findViewById(R.id.word9));
        mEditWords.add((EditText) findViewById(R.id.word10));
        mEditWords.add((EditText) findViewById(R.id.word11));
        mEditWords.add((EditText) findViewById(R.id.word12));

        for (EditText editText : mEditWords) {
            editText.setOnFocusChangeListener(this);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mIsRestoring = extras.getBoolean(EXTRA_UNLINK);
            mIsResettingPin = extras.getBoolean(EXTRA_RESET_PIN);
        }

        if (mIsRestoring) {
            //change the labels
            title.setText(getString(R.string.MenuViewController_recoverButton));
            description.setText(getString(R.string.WipeWallet_instruction));
        } else if (mIsResettingPin) {
            //change the labels
            title.setText(getString(R.string.RecoverWallet_header_reset_pin));
            description.setText(getString(R.string.RecoverWallet_subheader_reset_pin));
        }


        mEditWords.get(LAST_WORD_INDEX).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    mNextButton.performClick();
                }
                return false;
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: next step1111");
                if (!BRAnimator.isClickAllowed()) return;
                final Activity app = InputWordsActivity.this;
                String phraseToCheck = getPhrase();
                if (Utils.isEmulatorOrDebug(app) && !Utils.isNullOrEmpty(mDebugPhrase)) {
                    phraseToCheck = mDebugPhrase;
                }
                if (phraseToCheck == null) {
                    return;
                }
                String cleanPhrase = SmartValidator.cleanPaperKey(app, phraseToCheck);
                if (Utils.isNullOrEmpty(cleanPhrase)) {
                    BRReportsManager.reportBug(new NullPointerException("cleanPhrase is null or empty!"));
                    return;
                }
                if (SmartValidator.isPaperKeyValid(app, cleanPhrase)) {

                    if (mIsRestoring || mIsResettingPin) {
                        if (SmartValidator.isPaperKeyCorrect(cleanPhrase, app)) {
                            Utils.hideKeyboard(app);
                            clearWords();

                            if (mIsRestoring) {

                                BRDialog.showCustomDialog(InputWordsActivity.this, getString(R.string.WipeWallet_alertTitle),
                                        getString(R.string.WipeWallet_alertMessage), getString(R.string.WipeWallet_wipe), getString(R.string.Button_cancel), new BRDialogView.BROnClickListener() {
                                            @Override
                                            public void onClick(BRDialogView brDialogView) {
                                                brDialogView.dismissWithAnimation();
                                                WalletsMaster m = WalletsMaster.getInstance(InputWordsActivity.this);
                                                m.wipeWalletButKeystore(app);
                                                m.wipeKeyStore(app);
                                                Intent intent = new Intent(app, IntroActivity.class);
                                                finalizeIntent(intent);
                                            }
                                        }, new BRDialogView.BROnClickListener() {
                                            @Override
                                            public void onClick(BRDialogView brDialogView) {
                                                brDialogView.dismissWithAnimation();
                                            }
                                        }, null, 0);

                            } else {
                                Log.i(TAG, "onClick: next step22222");
                                AuthManager.getInstance().setPinCode("", InputWordsActivity.this);
                                Intent intent = new Intent(app, SetPinActivity.class);
                                intent.putExtra("noPin", true);
                                finalizeIntent(intent);
                            }


                        } else {
                            BRDialog.showCustomDialog(app, "", getString(R.string.RecoverWallet_invalid),
                                    getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                        @Override
                                        public void onClick(BRDialogView brDialogView) {
                                            brDialogView.dismissWithAnimation();
                                        }
                                    }, null, null, 0);
                        }

                    } else {
                        Utils.hideKeyboard(app);
                        WalletsMaster m = WalletsMaster.getInstance(InputWordsActivity.this);
                        m.wipeAll(InputWordsActivity.this);
                        PostAuth.getInstance().setCachedPaperKey(cleanPhrase);
                        //Disallow BTC and BCH sending.
                        BRSharedPrefs.putAllowSpend(app, BaseBitcoinWalletManager.BITCASH_SYMBOL, false);
                        BRSharedPrefs.putAllowSpend(app, BaseBitcoinWalletManager.BITCOIN_SYMBOL, false);

                        PostAuth.getInstance().onRecoverWalletAuth(app, false);
                    }

                } else {
                    BRDialog.showCustomDialog(app, "", getResources().getString(R.string.RecoverWallet_invalid),
                            getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);

                }
            }
        });

    }

    private void finalizeIntent(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        startActivity(intent);
        if (!InputWordsActivity.this.isDestroyed()) finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private String getPhrase() {
        boolean success = true;

        StringBuilder paperKeyStringBuilder = new StringBuilder();

        for (EditText editText : mEditWords) {
            String cleanedWords = clean(editText.getText().toString().toLowerCase());
            if (Utils.isNullOrEmpty(cleanedWords)) {
                SpringAnimator.failShakeAnimation(this, editText);
                success = false;
            } else {
                paperKeyStringBuilder.append(cleanedWords);
                paperKeyStringBuilder.append(' ');
            }
        }
        //remove the last space
        paperKeyStringBuilder.setLength(paperKeyStringBuilder.length() - 1);


        String paperKey = paperKeyStringBuilder.toString();

        if (!success) {
            return null;
        }

        //ensure the paper key is 12 words
        int numberOfWords = paperKey.split(" ").length;
        if (numberOfWords != NUMBER_OF_WORDS) {
            BRReportsManager.reportBug(new IllegalArgumentException("Paper key contains " + numberOfWords + " words"));
            return null;
        }

        return paperKey;
    }

    private String clean(String word) {
        return word.trim().replaceAll(" ", "");
    }

    private void clearWords() {
        for (EditText editText : mEditWords) {
            editText.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            validateWord((EditText) v);
        } else {
            ((EditText) v).setTextColor(getColor(R.color.light_gray));
        }
    }

    private void validateWord(EditText view) {
        String word = view.getText().toString();
        boolean valid = SmartValidator.isWordValid(this, word);
        view.setTextColor(getColor(valid ? R.color.light_gray : R.color.red_text));
        if (!valid) {
            SpringAnimator.failShakeAnimation(this, view);
        }
    }

}
