package com.groep9.apex.apexandroid.Advies;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.groep9.apex.apexandroid.R;

import java.util.List;

public class RVAdapterAdvies extends RecyclerView.Adapter<RVAdapterAdvies.CardViewHolderAdvies> {

    public static class CardViewHolderAdvies extends RecyclerView.ViewHolder {
        CardView cv;
        TextView cardTitle;
        TextView cardContent;
        ImageView image;

        CardViewHolderAdvies(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view_item_advies);
            cardTitle = (TextView) itemView.findViewById(R.id.tv_title_card_view_item_advies);
            cardContent = (TextView) itemView.findViewById(R.id.tv_content_card_view_item_advies);
            image = (ImageView) itemView.findViewById(R.id.iv_card_view_item_advies);
        }
    }

    List<CardItemAdvies> cards;

    RVAdapterAdvies(List<CardItemAdvies> cards) {
        this.cards = cards;
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public CardViewHolderAdvies onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_item_advies, viewGroup, false);
        return new CardViewHolderAdvies(v);
    }

    @Override
    public void onBindViewHolder(CardViewHolderAdvies cardViewHolderAdvies, int i) {
        CardItemAdvies card = cards.get(i);

        cardViewHolderAdvies.cardTitle.setText(card.getTitle());
        cardViewHolderAdvies.cardContent.setText(card.getContent());
        cardViewHolderAdvies.image.setImageResource(card.getImageId());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
