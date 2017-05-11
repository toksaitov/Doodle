package com.toksaitov.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

public class ItemDetailFragment extends Fragment {

    public static final String ARG_ITEM_PATH = "item_path";

    private File file;

    private DrawingView drawingView;

    public ItemDetailFragment() {
        drawingView = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle argumnets = getArguments();
        if (argumnets.containsKey(ARG_ITEM_PATH)) {
            String path = argumnets.getString(ARG_ITEM_PATH);
            if (path != null) {
                file = new File(path);

                Activity activity = this.getActivity();
                Toolbar toolbar = (Toolbar) activity.findViewById(R.id.detail_toolbar);
                if (toolbar != null) {
                    toolbar.setTitle(file.getName());
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);
        drawingView = (DrawingView) rootView.findViewById(R.id.drawingView);
        drawingView.loadBitmap(file);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (drawingView != null && drawingView.shouldSave) {
            if (!drawingView.saveBitmap(file)) {
                reportError("Failed to save changes");
            }
        }
    }

    private void reportError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
