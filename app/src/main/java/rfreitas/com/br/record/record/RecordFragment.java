package rfreitas.com.br.record.record;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import rfreitas.com.br.record.R;

public class RecordFragment extends Fragment implements RecordView {

    @BindView(R.id.tempo)
    TextView tempo;

    @BindView(R.id.status)
    TextView status;

    @BindView(R.id.btn_record)
    Button gravar;

    private Toast timeIsReachedToast, errorToast, stopToast;

    private RecordPresenter presenter;
    private Animation pulse;

    private Timer timer;
    private int seconds = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recorder_fragment, container, false);
        ButterKnife.bind(this, view);

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecordPresenter presenter = new RecordPresenter();
        presenter.setView(this);
        presenter.setRecordPresenterListener(getPresenterListener());

        setPresenter(presenter);

        pulse = AnimationUtils.loadAnimation(getActivity(), R.anim.pulse);
        gravar.setOnTouchListener(getOnTouchListener());
    }

    public void setPresenter(RecordPresenter presenter) {
        this.presenter = presenter;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public View.OnTouchListener getOnTouchListener(){
        return new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        status.setText(R.string.record_status_message);
                        status.setTextColor(Color.BLUE);

                        tempo.setAnimation(getPulse());
                        presenter.startRecord(getActivity().getFilesDir().getPath(), UUID.randomUUID().toString());

                        return true;
                    case MotionEvent.ACTION_UP:

                        status.setText(R.string.default_status_message);
                        status.setTextColor(Color.BLACK);

                        tempo.clearAnimation();
                        presenter.stopRecord();

                        return true;
                }

                return false;
            }

        };
    }

    public RecordPresenter.RecordPresenterListener getPresenterListener() {
        return new RecordPresenter.RecordPresenterListener() {

            @Override
            public void onInitRecord() {
                showProgressOfRecord();
            }

            @Override
            public void onStopRecord() {
                stopProgressOfRecord();
            }

        };
    }

    @Override
    public void showProgressOfRecord() {
        timer = getNewTimer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tempo.setText(DateUtils.formatElapsedTime(seconds++));
                    }

                });

            }

        }, 0, 1000);

        tempo.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopProgressOfRecord() {
        if(stopToast == null || stopToast.getView().isShown()){
            stopToast = Toast.makeText(getActivity(), "A gravação encerrou", Toast.LENGTH_SHORT);
            stopToast.show();
        }

        reload();

    }

    @Override
    public void showError(String error) {
        if(errorToast == null || errorToast.getView().isShown()){
            errorToast = Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT);
            errorToast.show();
        }

        reload();
    }

    @Override
    public void showTimeIsReached() {
        if(timeIsReachedToast == null || timeIsReachedToast.getView().isShown()){
            timeIsReachedToast = Toast.makeText(getActivity(), "O tempo máximo de gravação encerrou.", Toast.LENGTH_SHORT);
            timeIsReachedToast.show();
        }

        reload();
    }

    @Override
    public void reload() {
        tempo.setVisibility(View.INVISIBLE);
        tempo.setText("00:00");

        seconds = 0;
        timer.cancel();
        timer.purge();
    }

    public Animation getPulse() {
        return pulse;
    }

    @Override
    public Activity getContextActivity() {
        return getActivity();
    }

    public Timer getNewTimer(){
        return new Timer();
    }

    public Toast getErrorToast() {
        return errorToast;
    }

    public Toast getStopToast() {
        return stopToast;
    }

    public Toast getTimeIsReachedToast() {
        return timeIsReachedToast;
    }

}