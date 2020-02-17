package edu.temple.mapchatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PartnerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Partner> partners;

    public PartnerListAdapter(Context context, ArrayList<Partner> partners) {
        this.context = context;
        this.partners = partners;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View row = layoutInflater.inflate(R.layout.partner_item, parent, false);
        partner p = new partner(row);
        return p;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((partner)holder).name.setText(partners.get(position).user);
        ((partner)holder).distance.setText(String.valueOf(Math.round(partners.get(position).distance)) + " m");
    }

    @Override
    public int getItemCount() {
        return partners.size();
    }

    public class partner extends RecyclerView.ViewHolder {
        TextView name;
        TextView distance;

        public partner(View partnerView) {
            super(partnerView);
            name = partnerView.findViewById(R.id.partnerName);
            distance = partnerView.findViewById(R.id.distance);
        }

    }
}
