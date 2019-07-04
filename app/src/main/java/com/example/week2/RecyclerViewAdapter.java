package com.example.week2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.LENGTH_SHORT;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mImageNames = new ArrayList<>();
    private ArrayList<Bitmap> mImages = new ArrayList<>();
    private ArrayList<String> mPhoneNo = new ArrayList<>();
    private Context mContext;
    int i = 0;


    public RecyclerViewAdapter(Context Context, ArrayList<String> ImageNames, ArrayList<Bitmap> Images, ArrayList<String> PhoneNo) {
        this.mImageNames = ImageNames;
        this.mImages = Images;
        this.mContext = Context;
        this.mPhoneNo = PhoneNo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.getAdapterPosition();

        Glide.with(mContext)
                .asBitmap()
                .load(mImages.get(position))
                .into(holder.image);
        holder.imageName.setText(mImageNames.get(position));
        if (holder.imageName.getText().length() > 13){
            holder.imageName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
        holder.phoneNo.setText(mPhoneNo.get(position));
        if (mPhoneNo.get(position) == "ADD PHONE NUMBER") {
            holder.phoneNo.setAlpha(0.2f);
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                i++;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (i==1){
                            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(mPhoneNo.get(position)));
                            String[] projection = new String[]{ ContactsContract.PhoneLookup._ID };

                            Cursor cur = mContext.getContentResolver().query(uri, projection, null, null, null);

                            if (cur != null && cur.moveToNext()) {
                                Long id = cur.getLong(0);

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id));
                                intent.setData(contactUri);
                                mContext.startActivity(intent);

                                cur.close();
                            }
                        }
                        i=0;
                    }
                }, 200);
            }
        });

    }



    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView imageName;
        TextView phoneNo;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            image = itemView.findViewById(R.id.image);
            imageName = itemView.findViewById(R.id.image_name);
            phoneNo = itemView.findViewById(R.id.phone_no);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

}
