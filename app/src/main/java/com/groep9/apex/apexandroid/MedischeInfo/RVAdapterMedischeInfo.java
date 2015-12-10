package com.groep9.apex.apexandroid.MedischeInfo;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.groep9.apex.apexandroid.R;

import java.util.List;

public class RVAdapterMedischeInfo extends RecyclerView.Adapter<RVAdapterMedischeInfo.CardViewHolderMedischeInfo> {

    public static class CardViewHolderMedischeInfo extends RecyclerView.ViewHolder {
        CardView cv;
        TextView cardTitle;
        TextView tv1;
        TextView tv2;
        TextView tv3;
        TextView tv4;
        ImageView image;

        CardViewHolderMedischeInfo(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view_item_medische_info);
            cardTitle = (TextView) itemView.findViewById(R.id.tv_title_card_view_item_medische_info);
            tv1 = (TextView) itemView.findViewById(R.id.tv1_card_view_item_medische_info);
            tv2 = (TextView) itemView.findViewById(R.id.tv2_card_view_item_medische_info);
            tv3 = (TextView) itemView.findViewById(R.id.tv3_card_view_item_medische_info);
            tv4 = (TextView) itemView.findViewById(R.id.tv4_card_view_item_medische_info);
            image = (ImageView) itemView.findViewById(R.id.iv_card_view_item_medische_info);
        }
    }

    List<CardItemMedischeInfo> cards;

    RVAdapterMedischeInfo(List<CardItemMedischeInfo> cards) {
        this.cards = cards;
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public CardViewHolderMedischeInfo onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_item_medische_info, viewGroup, false);
        return new CardViewHolderMedischeInfo(v);
    }

    @Override
    public void onBindViewHolder(CardViewHolderMedischeInfo cardViewHolderMedischeInfo, int i) {
        CardItemMedischeInfo card = cards.get(i);

        cardViewHolderMedischeInfo.cardTitle.setText(card.getTitle());
        cardViewHolderMedischeInfo.tv1.setText(card.getContent1());
        cardViewHolderMedischeInfo.tv2.setText(card.getContent2());

        if (card.getContent3() != null) {
            cardViewHolderMedischeInfo.tv3.setVisibility(View.VISIBLE);
            cardViewHolderMedischeInfo.tv3.setText(card.getContent3());
        }

        if (card.getContent4() != null) {
            cardViewHolderMedischeInfo.tv4.setVisibility(View.VISIBLE);
            cardViewHolderMedischeInfo.tv4.setText(card.getContent4());
        }

        cardViewHolderMedischeInfo.image.setImageResource(cards.get(i).getImageId());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
