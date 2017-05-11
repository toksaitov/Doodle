package com.toksaitov.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

public class ItemListActivity extends AppCompatActivity {

    private static final String DRAWINGS_DIRECTORY_NAME = "drawings";

    private boolean hasTwoPanes;

    private SimpleItemRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(getTitle());
        }

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.item_list);
        if (recyclerView != null) {
            recyclerViewAdapter = new SimpleItemRecyclerViewAdapter();
            recyclerView.setAdapter(recyclerViewAdapter);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                File drawingsDirectory =
                    getDir(DRAWINGS_DIRECTORY_NAME, MODE_PRIVATE);
                String path =
                    drawingsDirectory.getAbsoluteFile() +
                        File.separator + UUID.randomUUID() + "_" +
                            System.currentTimeMillis();

                File drawing = new File(path);
                try {
                    drawing.createNewFile();
                } catch (IOException e) {
                    reportError("Failed to create a new image");
                    return;
                }

                if (hasTwoPanes) {
                    Bundle arguments = new Bundle();
                    arguments.putString(
                        ItemDetailFragment.ARG_ITEM_PATH,
                        path
                    );

                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);

                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();

                    recyclerViewAdapter.update();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(
                        ItemDetailFragment.ARG_ITEM_PATH,
                        path
                    );

                    context.startActivity(intent);
                }

                recyclerViewAdapter.notifyItemInserted(recyclerViewAdapter.getItemCount());
                }
            });
        }

        hasTwoPanes = findViewById(R.id.item_detail_container) != null;
    }

    private void reportError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            final View view;
            final TextView contentView;

            File drawing;

            ViewHolder(View view) {
                super(view);

                this.view = view;
                contentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + drawing.getName() + "'";
            }
        }

        private File[] drawings;

        SimpleItemRecyclerViewAdapter() {
            update();
        }

        final void update() {
            File drawingsDirectory = getDir(DRAWINGS_DIRECTORY_NAME, MODE_PRIVATE);
            drawings = sortFilesByCreationTime(drawingsDirectory.listFiles());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_content, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (position >= drawings.length) {
                update();
            }

            final File drawing = drawings[position];

            holder.drawing = drawing;
            holder.contentView.setText(drawing.getName());

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasTwoPanes) {
                        Bundle arguments = new Bundle();
                        arguments.putString(
                            ItemDetailFragment.ARG_ITEM_PATH,
                            drawing.getAbsolutePath()
                        );

                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);

                        getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                    } else {
                        Context context = v.getContext();

                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(
                            ItemDetailFragment.ARG_ITEM_PATH,
                            drawing.getAbsolutePath()
                        );

                        context.startActivity(intent);
                    }
                }
            });

            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!drawing.delete()) {
                        reportError("Failed to remove the image");
                    } else {
                        update(); notifyItemRemoved(holder.getAdapterPosition());
                    }

                    return true;
                }
            });

            setFadeAnimation(holder.view);
        }

        @Override
        public int getItemCount() {
            return drawings.length;
        }

        private File[] sortFilesByCreationTime(File[] files) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File firstFile, File secondFile)  {
                    String[] parts;

                    String firstFileFileName = firstFile.getName();
                    parts = firstFileFileName.split("_");
                    long firstTimeStamp = 0;
                    try {
                        firstTimeStamp = Long.parseLong(parts[1]);
                    } catch(NumberFormatException e) {
                        e.printStackTrace();
                    }

                    String secondFileFileName = secondFile.getName();
                    parts = secondFileFileName.split("_");
                    long secondTimeStamp = 0;
                    try {
                        secondTimeStamp = Long.parseLong(parts[1]);
                    } catch(NumberFormatException e) {
                        e.printStackTrace();
                    }

                    return Long.valueOf(firstTimeStamp).compareTo(secondTimeStamp);
                }
            });

            return files;
        }

        private void setFadeAnimation(View view) {
            final int ANIMATION_DURATION = 1000;

            AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(ANIMATION_DURATION);

            view.startAnimation(animation);
        }

    }

}
