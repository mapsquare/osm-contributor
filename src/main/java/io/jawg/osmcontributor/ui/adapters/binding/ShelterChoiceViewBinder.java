package io.jawg.osmcontributor.ui.adapters.binding;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.ui.adapters.item.ShelterTagItem;
import io.jawg.osmcontributor.ui.adapters.item.ShelterType;
import io.jawg.osmcontributor.ui.adapters.item.TagItem;
import io.jawg.osmcontributor.ui.utils.views.holders.ShelterChoiceHolder;


public class ShelterChoiceViewBinder extends CheckedTagViewBinder<ShelterChoiceHolder, ShelterTagItem> {

    public ShelterChoiceViewBinder(Activity activity, OnTagItemChange onTagItemChange) {
        super(activity, onTagItemChange);
        ((OsmTemplateApplication) activity.getApplication()).getOsmTemplateComponent().inject(this);
    }

    @Override
    public boolean supports(TagItem.Type type) {
        return TagItem.Type.SHELTER.equals(type);
    }

    @Override
    public void onBindViewHolder(final ShelterChoiceHolder holder, final ShelterTagItem tagItem) {
        // Save holder
        this.content = holder.getContent();

        holder.setSelectionListener(new ShelterChoiceHolder.SelectionListener() {
                                        @Override
                                        public void shelterClicked(ShelterType shelterType) {
                                            tagItem.setShelterType(tagItem.getShelterType() == shelterType ? ShelterType.UNDEFINED : shelterType);
                                            updateImages(holder, tagItem);
                                            if (onTagItemChange != null) {
                                                onTagItemChange.onTagItemUpdated(tagItem);
                                            }
                                        }
                                    }
        );

        // if Tag is show=false, hide it
        if (!tagItem.isShow()) {
            ((RelativeLayout) holder.getContent().getParent()).setVisibility(View.GONE);
        }

        updateImages(holder, tagItem);

        // run validation process
        showInvalidityMessage(tagItem);
    }

    private void updateImages(ShelterChoiceHolder holder, ShelterTagItem tagItem) {
        ShelterType shelterType = tagItem.getShelterType();

        holder.getNoneImg().setImageDrawable(activity.get().getResources().getDrawable(shelterType.equals(ShelterType.NONE) ? R.drawable.no_shelter_on : R.drawable.no_shelter_off));
        holder.getPoleImg().setImageDrawable(activity.get().getResources().getDrawable(shelterType.equals(ShelterType.POLE) ? R.drawable.pole_shelter_on : R.drawable.pole_shelter_off));
        holder.getShelterImg().setImageDrawable(activity.get().getResources().getDrawable(shelterType.equals(ShelterType.SHELTER) ? R.drawable.has_shelter_on : R.drawable.has_shelter_off));

    }

    @Override
    public ShelterChoiceHolder onCreateViewHolder(ViewGroup parent) {
        View booleanChoiceLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item_shelter, parent, false);
        return new ShelterChoiceHolder(booleanChoiceLayout);
    }
}
