package com.example.concertticket;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ConcertTicketAdapter extends RecyclerView.Adapter<ConcertTicketAdapter.ViewHolder> implements Filterable {

    private ArrayList<ConcertTicket> concertTicketItemData;
    private ArrayList<ConcertTicket> concertTicketItemDataAll;
    private Context context;
    private int lastPos = -1;

    ConcertTicketAdapter(Context context, ArrayList<ConcertTicket> itemData){
        this.concertTicketItemData = itemData;
        this.concertTicketItemDataAll = itemData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ConcertTicketAdapter.ViewHolder holder, int position) {
        ConcertTicket currentItem = concertTicketItemData.get(position);
        holder.bindTo(currentItem);

        if(holder.getAdapterPosition() > lastPos) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in);
            holder.itemView.startAnimation(animation);
            lastPos = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() { return concertTicketItemData.size(); }

    @Override
    public Filter getFilter() { return concertFilter; }

    private Filter concertFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<ConcertTicket> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length()  == 0) {
                results.count = concertTicketItemDataAll.size();
                results.values = concertTicketItemDataAll;
            } else {
                String filter = constraint.toString().toLowerCase().trim();

                for(ConcertTicket ticket : concertTicketItemDataAll){
                    if(ticket.getTitle().toLowerCase().contains(filter)){
                        filteredList.add(ticket);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            concertTicketItemData = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView titleText;
        private TextView descriptionText;
        private TextView priceText;
        private ImageView itemImage;

        public ViewHolder(View itemView) {
            super(itemView);

            titleText = itemView.findViewById(R.id.concertTitle);
            descriptionText = itemView.findViewById(R.id.description);
            priceText = itemView.findViewById(R.id.price);
            itemImage = itemView.findViewById(R.id.concertImage);

            itemView.findViewById(R.id.add_to_cart).setOnClickListener(v -> Log.d("Activity", "Button clicked"));
        }

        public void bindTo(ConcertTicket currentItem) {
            titleText.setText(currentItem.getTitle());
            descriptionText.setText(currentItem.getDescription());
            priceText.setText(currentItem.getPrice());
            Glide.with(context).load(currentItem.getImageResource()).into(itemImage);
            itemView.findViewById(R.id.delete).setOnClickListener(v -> ((ConcertTicketListActivity)context).deleteItem(currentItem));
        }
    }
}
