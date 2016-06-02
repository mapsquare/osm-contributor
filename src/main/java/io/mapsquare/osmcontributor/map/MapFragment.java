/**
 * Copyright (C) 2016 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mapsquare.osmcontributor.map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.events.NodeRefAroundLoadedEvent;
import io.mapsquare.osmcontributor.core.events.PleaseDeletePoiEvent;
import io.mapsquare.osmcontributor.core.events.PleaseRemoveArpiMarkerEvent;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.core.model.PoiTypeTag;
import io.mapsquare.osmcontributor.edition.EditPoiActivity;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyNodeRefPositionChange;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyPoiPositionChange;
import io.mapsquare.osmcontributor.map.events.AddressFoundEvent;
import io.mapsquare.osmcontributor.map.events.ChangeMapModeEvent;
import io.mapsquare.osmcontributor.map.events.EditionWaysLoadedEvent;
import io.mapsquare.osmcontributor.map.events.LastUsePoiTypeLoaded;
import io.mapsquare.osmcontributor.map.events.MapCenterValueEvent;
import io.mapsquare.osmcontributor.map.events.NewNoteCreatedEvent;
import io.mapsquare.osmcontributor.map.events.NewPoiTypeSelected;
import io.mapsquare.osmcontributor.map.events.OnBackPressedMapEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyNoteFilterEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyPoiFilter;
import io.mapsquare.osmcontributor.map.events.PleaseChangePoiPosition;
import io.mapsquare.osmcontributor.map.events.PleaseChangeToolbarColor;
import io.mapsquare.osmcontributor.map.events.PleaseChangeValuesDetailNoteFragmentEvent;
import io.mapsquare.osmcontributor.map.events.PleaseChangeValuesDetailPoiFragmentEvent;
import io.mapsquare.osmcontributor.map.events.PleaseCreateNoTagPoiEvent;
import io.mapsquare.osmcontributor.map.events.PleaseDeletePoiFromMapEvent;
import io.mapsquare.osmcontributor.map.events.PleaseDisplayTutorialEvent;
import io.mapsquare.osmcontributor.map.events.PleaseGiveMeMapCenterEvent;
import io.mapsquare.osmcontributor.map.events.PleaseInitializeArpiEvent;
import io.mapsquare.osmcontributor.map.events.PleaseInitializeNoteDrawerEvent;
import io.mapsquare.osmcontributor.map.events.PleaseLoadEditWaysEvent;
import io.mapsquare.osmcontributor.map.events.PleaseLoadLastUsedPoiType;
import io.mapsquare.osmcontributor.map.events.PleaseOpenEditionEvent;
import io.mapsquare.osmcontributor.map.events.PleaseShowMeArpiglEvent;
import io.mapsquare.osmcontributor.map.events.PleaseSwitchWayEditionModeEvent;
import io.mapsquare.osmcontributor.map.events.PleaseToggleArpiEvent;
import io.mapsquare.osmcontributor.map.events.PleaseToggleDrawer;
import io.mapsquare.osmcontributor.map.events.PleaseToggleDrawerLock;
import io.mapsquare.osmcontributor.map.events.PoiNoTypeCreated;
import io.mapsquare.osmcontributor.map.listener.MapboxListener;
import io.mapsquare.osmcontributor.map.marker.LocationMarker;
import io.mapsquare.osmcontributor.map.marker.LocationMarkerOptions;
import io.mapsquare.osmcontributor.map.ways.Geocoder;
import io.mapsquare.osmcontributor.map.ways.LevelBar;
import io.mapsquare.osmcontributor.map.ways.VectorialObject;
import io.mapsquare.osmcontributor.map.ways.Way;
import io.mapsquare.osmcontributor.note.NoteCommentDialogFragment;
import io.mapsquare.osmcontributor.note.events.ApplyNewCommentFailedEvent;
import io.mapsquare.osmcontributor.sync.events.SyncDownloadWayEvent;
import io.mapsquare.osmcontributor.sync.events.SyncFinishUploadPoiEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncConflictingNodeErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncDownloadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncNewNodeErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUnauthorizedEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUploadNoteRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUploadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.TooManyRequestsEvent;
import io.mapsquare.osmcontributor.utils.FlavorUtils;
import io.mapsquare.osmcontributor.utils.StringUtils;
import timber.log.Timber;

@SuppressWarnings("all")
public class MapFragment extends Fragment {

    public static final String MAP_FRAGMENT_TAG = "MAP_FRAGMENT_TAG";
    private static final String LOCATION = "location";
    private static final String ZOOM_LEVEL = "zoom level";
    private static final String LEVEL = "level";
    private static final String MARKER_TYPE = "MARKER_TYPE";
    public static final String CREATION_MODE = "CREATION_MODE";
    public static final String POI_TYPE_ID = "POI_TYPE_ID";
    public static final String SELECTED_MARKER_ID = "SELECTED_MARKER_ID";
    public static final String HIDDEN_POI_TYPE = "HIDDEN_POI_TYPE";
    private static final String DISPLAY_OPEN_NOTES = "DISPLAY_OPEN_NOTES";
    private static final String DISPLAY_CLOSED_NOTES = "DISPLAY_CLOSED_NOTES";

    private LocationMarker markerSelected = null;

    // when resuming app we use this id to re-select the good marker
    private Long markerSelectedId = -1L;
    private MapMode mapMode = MapMode.DEFAULT;

    private boolean isMenuLoaded = false;
    private boolean pleaseSwitchToPoiSelected = false;

    private Map<Long, LocationMarkerOptions<Poi>> markersPoi;
    private Map<Long, LocationMarkerOptions<Note>> markersNotes;
    private Map<Long, LocationMarkerOptions<PoiNodeRef>> markersNodeRef;

    private int maxPoiType;
    private PoiType poiTypeSelected;
    private ButteryProgressBar progressBar;
    private MapFragmentPresenter presenter;
    private MapboxListener mapboxListener;

    private MapboxMap mapboxMap;

    private Unbinder unbinder;

    @Inject
    BitmapHandler bitmapHandler;

    @BindView(R.id.mapview)
    MapView mapView;

    @Inject
    EventBus eventBus;

    @BindView(R.id.poi_detail_wrapper)
    RelativeLayout poiDetailWrapper;

    @BindView(R.id.progressbar)
    RelativeLayout progressbarWrapper;

    @BindView(R.id.note_detail_wrapper)
    RelativeLayout noteDetailWrapper;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    ConfigManager configManager;

    //olduv
    //For testing purpose
    @BindView(R.id.zoom_level)
    TextView zoomLevelText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);

        tracker = ((OsmTemplateApplication) this.getActivity().getApplication()).getTracker(OsmTemplateApplication.TrackerName.APP_TRACKER);
        tracker.setScreenName("MapView");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        measureMaxPoiType();

        presenter = new MapFragmentPresenter(this);
        mapboxListener = new MapboxListener(this, eventBus);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        markersPoi = new HashMap<>();
        markersNotes = new HashMap<>();
        markersNodeRef = new HashMap<>();

        unbinder = ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);

        zoomVectorial = configManager.getZoomVectorial();

        if (savedInstanceState != null) {
            currentLevel = savedInstanceState.getDouble(LEVEL);
            selectedMarkerType = LocationMarker.MarkerType.values()[savedInstanceState.getInt(MARKER_TYPE)];
        }

        instantiateProgressBar();
        instantiateMapView(savedInstanceState);
        instantiateLevelBar();
        instantiatePoiTypePicker();

        eventBus.register(this);
        eventBus.register(presenter);
        return rootView;
    }

    private void instantiateProgressBar() {
        progressBar = new ButteryProgressBar(getActivity());
        progressBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 24));
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        p.addRule(RelativeLayout.BELOW, R.id.poi_type_value_wrapper);
        progressBar.setVisibility(View.GONE);
        progressBar.setLayoutParams(p);
        progressbarWrapper.addView(progressBar);
    }

    private void instantiateLevelBar() {
        Drawable d = getResources().getDrawable(R.drawable.level_thumb);
        levelBar.setThumb(d);
        levelBar.setDrawableHeight(d.getIntrinsicHeight());
        levelBar.setLevel(currentLevel);
        levelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Timber.v("onProgressChanged");
//                if (vectorialOverlay != null) {
//                    LevelBar lvl = (LevelBar) seekBar;
//                    currentLevel = lvl.getLevel();
//                    vectorialOverlay.setLevel(currentLevel);
//                    invalidateMap();
//                    applyPoiFilter();
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void instantiateMapBox(Bundle savedInstanceState) {
        mapboxMap.setMyLocationEnabled(true);

        // Set the map center and zoom to the saved values or use the default values
        Location myLocation = mapboxMap.getMyLocation();
        mapboxMap.setMyLocationEnabled(false);
        CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
        if (savedInstanceState == null) {
            cameraBuilder.target((FlavorUtils.isStore() && myLocation != null) ?
                    new LatLng(myLocation.getLatitude(), myLocation.getLongitude()) : configManager.getDefaultCenter())
                    .zoom(configManager.getDefaultZoom());
        } else {
            cameraBuilder.target((LatLng) savedInstanceState.getParcelable(LOCATION))
                    .zoom(savedInstanceState.getFloat(ZOOM_LEVEL));
        }

        mapboxMap.setCameraPosition(cameraBuilder.build());
        onResume();
        switchMode(MapMode.DEFAULT);

        mapboxListener.listen(mapboxMap);
    }

    private void instantiateMapView(final Bundle savedInstanceState) {
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.setStyleUrl("asset://osmMapStyle.json");
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    MapFragment.this.mapboxMap = mapboxMap;
                    instantiateMapBox(savedInstanceState);
                }
            });
        }
    }

    private void drawBounds() {
//        BoxOverlay boxOverlay = new BoxOverlay(configManager.getLatLngBounds());
//        mapView.addOverlay(boxOverlay);
    }

    private void measureMaxPoiType() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float toolbarSize = getResources().getDimension(R.dimen.abc_action_bar_default_height_material) / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        // 80 size of one floating btn in dp
        // 180 size of the menu btn plus location btn
        // -1 because we always have note

        maxPoiType = (int) ((dpHeight - toolbarSize - 160) / 80) - 1;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            Integer creationModeInt = savedInstanceState.getInt(CREATION_MODE);
            savePoiTypeId = savedInstanceState.getLong(POI_TYPE_ID);

            markerSelectedId = savedInstanceState.getLong(SELECTED_MARKER_ID, -1);

            mapMode = MapMode.values()[creationModeInt];
            if (mapMode == MapMode.DETAIL_NOTE || mapMode == MapMode.DETAIL_POI || mapMode == MapMode.POI_POSITION_EDITION) {
                mapMode = MapMode.DEFAULT;
            } else if (mapMode == MapMode.NODE_REF_POSITION_EDITION) {
                mapMode = MapMode.WAY_EDITION;
            }

            long[] hidden = savedInstanceState.getLongArray(HIDDEN_POI_TYPE);
            if (hidden != null) {
                for (long l : hidden) {
                    poiTypeHidden.add(l);
                }
            }

            displayOpenNotes = savedInstanceState.getBoolean(DISPLAY_OPEN_NOTES);
            displayClosedNotes = savedInstanceState.getBoolean(DISPLAY_CLOSED_NOTES);
//            switchToTileSource(savedInstanceState.getString(TILE_SOURCE));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //enable geolocation of user
        if (mapboxMap != null) {
            eventBus.post(new PleaseInitializeArpiEvent());
            presenter.setForceRefreshPoi();
            presenter.setForceRefreshNotes();
            presenter.loadPoisIfNeeded();
            eventBus.post(new PleaseInitializeNoteDrawerEvent(displayOpenNotes, displayClosedNotes));
            if (poiTypePickerAdapter != null) {
                poiTypePickerAdapter.setExpertMode(sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false));
            }
        }
    }

    @Override
    public void onPause() {
        if (mapboxMap != null) {
            mapboxMap.setMyLocationEnabled(false);
        }
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clear bitmapHandler even if activity leaks.
        bitmapHandler = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressBar.removeListeners();
        unbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LOCATION, mapboxMap.getCameraPosition().target);
        outState.putFloat(ZOOM_LEVEL, getZoomLevel());
        outState.putInt(CREATION_MODE, mapMode.ordinal());
        outState.putLong(POI_TYPE_ID, poiTypeSelected == null ? -1 : poiTypeSelected.getId());
        outState.putDouble(LEVEL, currentLevel);
        outState.putBoolean(DISPLAY_OPEN_NOTES, displayOpenNotes);
        outState.putBoolean(DISPLAY_CLOSED_NOTES, displayClosedNotes);
//        outState.putString(TILE_SOURCE, currentTileLayer);

        int markerType = markerSelected == null ? LocationMarker.MarkerType.NONE.ordinal() : markerSelected.getType().ordinal();
        outState.putInt(MARKER_TYPE, markerType);

        if (markerSelected != null) {
            switch (markerSelected.getType()) {
                case POI:
                    markerSelectedId = ((Poi) markerSelected.getRelatedObject()).getId();
                    break;
                case NODE_REF:
                    markerSelectedId = ((PoiNodeRef) markerSelected.getRelatedObject()).getId();
                    break;
                case NOTE:
                    markerSelectedId = ((Note) markerSelected.getRelatedObject()).getId();
                    break;
            }
        } else {
            markerSelectedId = -1L;
        }
        outState.putLong(SELECTED_MARKER_ID, markerSelectedId);


        long[] hidden = new long[poiTypeHidden.size()];
        int index = 0;

        for (Long value : poiTypeHidden) {
            hidden[index] = value;
            index++;
        }

        outState.putLongArray(HIDDEN_POI_TYPE, hidden);
    }

    /*-----------------------------------------------------------
    * ANALYTICS ATTRIBUTES
    *---------------------------------------------------------*/

    private Tracker tracker;

    private enum Category {
        MapMode("Map Mode"),
        GeoLocation("Geolocation"),
        Edition("Edition POI");

        private final String value;

        Category(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    /*-----------------------------------------------------------
    * ACTIONBAR
    *---------------------------------------------------------*/

    private MenuItem filter;
    private MenuItem confirm;
    private MenuItem downloadArea;
    private boolean homeActionBtnMode = true;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        displayHomeButton(true); //show the home button
        confirm = menu.findItem(R.id.action_confirm_position);
        downloadArea = menu.findItem(R.id.action_download_area);
        filter = menu.findItem(R.id.action_filter_drawer);
        // the menu has too be created because we modify it depending on mode
        isMenuLoaded = true;
        if (pleaseSwitchToPoiSelected) {
            pleaseSwitchToPoiSelected = false;
            mapboxListener.onMarkerClick(markerSelected);
        } else {
            //switchMode(mapMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_confirm_position:
                confirmPosition();
                break;

            case R.id.action_download_area:
                onDownloadZoneClick();
                break;

            case android.R.id.home:
                if (homeActionBtnMode) {
                    eventBus.post(new PleaseToggleDrawer());
                } else {
                    eventBus.post(new OnBackPressedMapEvent());
                }
                break;

            default:
                super.onOptionsItemSelected(item);
                break;
        }


        return true;
    }

    private void confirmPosition() {
        LatLng newPoiPosition;
        LatLng pos;

        switch (mapMode) {
            case POI_CREATION:
                createPoi();
                break;

            case NOTE_CREATION:
                pos = mapboxMap.getCameraPosition().target;
                NoteCommentDialogFragment dialog = NoteCommentDialogFragment.newInstance(pos.getLatitude(), pos.getLongitude());
                dialog.show(getActivity().getFragmentManager(), "dialog");
                break;

            case POI_POSITION_EDITION:
                Poi poi = (Poi) markerSelected.getRelatedObject();
                newPoiPosition = mapboxMap.getCameraPosition().target;
                eventBus.post(new PleaseApplyPoiPositionChange(newPoiPosition, poi.getId()));
                markerSelected.setPosition(newPoiPosition);
                markerSelected.setIcon(IconFactory.getInstance(getActivity()).fromBitmap(bitmapHandler.getMarkerBitmap(poi.getType(), Poi.computeState(false, false, true))));
                poi.setUpdated(true);
                mapboxMap.updateMarker(markerSelected);
                switchMode(MapMode.DETAIL_POI);
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Category.Edition.getValue())
                        .setAction("POI position edited")
                        .build());
                break;

            case NODE_REF_POSITION_EDITION:
                PoiNodeRef poiNodeRef = (PoiNodeRef) markerSelected.getRelatedObject();
                newPoiPosition = mapboxMap.getCameraPosition().target;
                eventBus.post(new PleaseApplyNodeRefPositionChange(newPoiPosition, poiNodeRef.getId()));
                markerSelected.setPosition(newPoiPosition);
                switchMode(MapMode.WAY_EDITION);
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Category.Edition.getValue())
                        .setAction("way point position edited")
                        .build());
                break;

            case DETAIL_POI:
                getActivity().finish();
                break;

            default:
                break;
        }
    }

    //if is back will show the back arrow, else will display the menu icon
    private void toggleBackButton(boolean homeActionBtnMode) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            this.homeActionBtnMode = homeActionBtnMode;
            if (homeActionBtnMode) {
                actionBar.setHomeAsUpIndicator(R.drawable.menu);
            } else {
                actionBar.setHomeAsUpIndicator(R.drawable.back);
            }
        }
    }

    private void displayHomeButton(boolean show) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(show);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOnBackPressedMapEvent(OnBackPressedMapEvent event) {
        Timber.d("Received event OnBackPressedMap");
        if (isTuto) {
            closeTuto();
        } else {
            switch (mapMode) {
                case POI_POSITION_EDITION:
                    mapboxMap.updateMarker(markerSelected);
                    switchMode(MapMode.DEFAULT);
                    break;

                case NODE_REF_POSITION_EDITION:
                    switchMode(MapMode.WAY_EDITION);
                    break;

                case WAY_EDITION:
                    clearVectorialEdition();
                    switchMode(MapMode.DEFAULT);
                    break;

                case DETAIL_POI:
                case DETAIL_NOTE:
                case POI_CREATION:
                case NOTE_CREATION:
                case TYPE_PICKER:
                    switchMode(MapMode.DEFAULT);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mapView.getWindowToken(), 0);
                    break;

                case ARPIGL:
                    switchMode(MapMode.DEFAULT);
                    eventBus.post(new PleaseToggleArpiEvent());
                    break;
                default:
                    getActivity().finish();
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseGiveMeMapCenterEvent(PleaseGiveMeMapCenterEvent event) {
        eventBus.post(new MapCenterValueEvent(mapboxMap.getCameraPosition().target));
    }


    public void switchMode(MapMode mode) {
        mapMode = mode;
        Bitmap bitmap;

        switchToolbarMode(mapMode);
        editNodeRefPosition.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        final MapMode.MapModeProperties properties = mode.getProperties();

        if (properties.isUnSelectIcon()) {
            unselectIcon();
        }

        showFloatingButtonAddPoi(properties.isShowAddPoiFab());
        displayPoiTypePicker();
        displayPoiDetailBanner(properties.isShowPoiBanner());
        displayNoteDetailBanner(properties.isShowNodeBanner());

        // If there must be a min zoom, take zoom vectorial
        // If there is no need for a zoom min, take the zoom min of the current TileProvider.
        /**
         * TODO
         */
//        mapboxMap.setMinZoom(properties.isZoomOutLimited() ? zoomVectorial : mapboxMap.getTileProvider().getMinimumZoomLevel());
//        mapboxMap.setMinZoom(zoomVectorial);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(Category.MapMode.getValue())
                .setAction(properties.getAnalyticsAction())
                .build());

        switch (mode) {
            case DETAIL_POI:
            case DETAIL_NOTE:
                break;

            case TYPE_PICKER:
                eventBus.post(new PleaseLoadLastUsedPoiType());
                break;

            case POI_CREATION:
                animationPoiCreation();
                break;

            case NOTE_CREATION:
                noteSelected();
                animationPoiCreation();
                break;

            case POI_POSITION_EDITION:
                // This marker is being moved
                bitmap = bitmapHandler.getMarkerBitmap(((Poi) markerSelected.getRelatedObject()).getType(), Poi.computeState(false, true, false));
                creationPin.setImageBitmap(bitmap);
                break;

            case NODE_REF_POSITION_EDITION:
                break;

            case WAY_EDITION:
                loadAreaForEdition();
//                if (vectorialOverlay != null) {
//                    vectorialOverlay.setMovingObjectId(null);
//                    vectorialOverlay.setSelectedObjectId(null);
//                }
//                else if (markerSelected == null && markerSelectedId != -1) {
//                    eventBus.post(new PleaseSelectNodeRefByID(markerSelectedId));
//                }
                break;

            default:
                poiTypeSelected = null;
                poiTypeEditText.setText("");
                addPoiFloatingButton.collapse();
                break;
        }

        //the marker is displayed at the end of the animation
        creationPin.setVisibility(properties.isShowCreationPin() ? View.VISIBLE : View.GONE);
    }

    private void switchToolbarMode(MapMode mode) {
        MapMode.MapModeProperties properties = mode.getProperties();

        confirm.setVisible(properties.isShowConfirmBtn());
        downloadArea.setVisible(properties.isShowDownloadArea());
        filter.setVisible(!properties.isLockDrawer());
        toggleBackButton(properties.isMenuBtn());
        getActivity().setTitle(properties.getTitle(getActivity()));
        eventBus.post(new PleaseChangeToolbarColor(properties.isEditColor()));
        eventBus.post(new PleaseToggleDrawerLock(properties.isLockDrawer()));
    }

    /**
     * Show or hide the {@link ButteryProgressBar}.
     *
     * @param show Whether we should show the progressBar.
     */
    public void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseSwitchWayEditionModeEvent(PleaseSwitchWayEditionModeEvent event) {
        if (getZoomLevel() < zoomVectorial) {
            changeMapZoomSmooth(zoomVectorial);
        }
        switchMode(MapMode.WAY_EDITION);
        downloadAreaForEdition();
    }

    /**
     * Download data from backend. If we are in {@link MapMode#WAY_EDITION}, download ways
     * and if not, download Pois.
     */
    public void onDownloadZoneClick() {
        if (mapMode == MapMode.WAY_EDITION) {
            downloadAreaForEdition();
        } else {
            // If flavor Store, allow the download only if the zoom > 18
            if (!FlavorUtils.isStore() || getZoomLevel() >= 16) {
                presenter.downloadAreaPoisAndNotes();
                Toast.makeText(getActivity(), R.string.download_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.zoom_more, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void defaultMap() {
        presenter.setForceRefreshPoi();
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
        switchMode(MapMode.DEFAULT);
    }

    public void unselectIcon() {
        if (markerSelected != null) {
            Bitmap bitmap = null;

            switch (markerSelected.getType()) {
                case POI:
                    Poi poi = (Poi) markerSelected.getRelatedObject();
                    bitmap = bitmapHandler.getMarkerBitmap(poi.getType(), Poi.computeState(false, false, poi.getUpdated()));
                    break;

                case NOTE:
                    bitmap = bitmapHandler.getNoteBitmap(Note.computeState((Note) markerSelected.getRelatedObject(), false, false));
                    break;

                case NODE_REF:
                    markerSelected.setIcon(IconFactory.getInstance(getActivity()).fromResource(R.drawable.waypoint));
                    break;
                default:
                    break;
            }
            if (bitmap != null) {
                markerSelected.setIcon(IconFactory.getInstance(getActivity()).fromBitmap(bitmap));
            }
            markerSelected = null;
        }
    }

    public boolean hasMarkers() {
        return !markersPoi.isEmpty();
    }

    public void removeAllMarkers() {
        for (Long markerId : markersNotes.keySet()) {
            removeMarker(markersNotes.get(markerId));
        }
        for (Long markerId : markersNodeRef.keySet()) {
            removeMarker(markersNodeRef.get(markerId));
        }
        markersNotes.clear();
        markersNodeRef.clear();
        removeAllPoiMarkers();
    }

    public void removeAllPoiMarkers() {
        for (Long markerId : markersPoi.keySet()) {
            removeMarker(markersPoi.get(markerId));
        }
        markersPoi.clear();
    }

    public void removePoiMarkersNotIn(List<Long> poiIds) {
        Set<Long> idsToRemove = new HashSet<>(markersPoi.keySet());
        idsToRemove.removeAll(poiIds);
        for (Long id : idsToRemove) {
            removeMarker(markersPoi.get(id));
        }
        markersPoi.keySet().removeAll(idsToRemove);
    }

    public void removeNoteMarkersNotIn(List<Long> noteIds) {
        Set<Long> idsToRemove = new HashSet<>(markersNotes.keySet());
        idsToRemove.removeAll(noteIds);
        for (Long id : idsToRemove) {
            removeMarker(markersNotes.get(id));
        }
        markersNotes.keySet().removeAll(idsToRemove);
    }

    public Map<Long, LocationMarkerOptions<Poi>> getMarkersPoi() {
        return markersPoi;
    }

    private void removeMarker(LocationMarkerOptions marker) {
        if (marker != null) {
            mapboxMap.removeMarker(marker.getMarker());
            Object poi = marker.getMarker().getRelatedObject();
            eventBus.post(new PleaseRemoveArpiMarkerEvent(poi));
        }
    }



    public void reselectMarker() {
        // if I found the marker selected I click on it
        if (markerSelected != null) {
            if (isMenuLoaded) {
                mapboxListener.onMarkerClick(markerSelected);
            } else {
                pleaseSwitchToPoiSelected = true;
            }
        }
    }

    public void addMarker(LocationMarkerOptions<Poi> marker) {
        markersPoi.put(marker.getMarker().getRelatedObject().getId(), marker);
        addPoiMarkerDependingOnFilters(marker);
    }

    public void invalidateMap() {
        mapView.invalidate();
    }

    public void addNote(LocationMarkerOptions<Note> marker) {
        markersNotes.put(marker.getMarker().getRelatedObject().getId(), marker);
        // add the note to the map
        addNoteMarkerDependingOnFilters(marker);
    }

    public LocationMarkerOptions<Note> getNote(Long id) {
        return markersNotes.get(id);
    }

    /*-----------------------------------------------------------
    * WAY EDITION
    *---------------------------------------------------------*/

    @BindView(R.id.edit_way_elemnt_position)
    FloatingActionButton editNodeRefPosition;


    @OnClick(R.id.edit_way_elemnt_position)
    public void setEditNodeRefPosition() {
//        vectorialOverlay.setMovingObjectId(((PoiNodeRef) markerSelected.getRelatedObject()).getNodeBackendId());
        switchMode(MapMode.NODE_REF_POSITION_EDITION);
    }

    //get data from overpass
    private void downloadAreaForEdition() {
        if (getZoomLevel() >= zoomVectorial) {
            progressBar.setVisibility(View.VISIBLE);
            LatLngBounds viewLatLngBounds = getViewLatLngBounds();
            eventBus.post(new SyncDownloadWayEvent(viewLatLngBounds));
        } else {
            Toast.makeText(getActivity(), getString(R.string.zoom_to_edit), Toast.LENGTH_SHORT).show();
        }
    }

    //get data from the bd
    private void loadAreaForEdition() {
        if (getZoomLevel() >= zoomVectorial) {
            eventBus.post(new PleaseLoadEditWaysEvent(false));
        } else {
            Toast.makeText(getActivity(), getString(R.string.zoom_to_edit), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAllNodeRef() {
        for (LocationMarkerOptions locationMarker : markersNodeRef.values()) {
            removeMarker(locationMarker);
        }
        markersNodeRef.clear();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNodeRefAroundLoadedEvent(NodeRefAroundLoadedEvent event) {
        PoiNodeRef poiNodeRefSelected = event.getPoiNodeRef();
        if (poiNodeRefSelected != null) {
            invalidateMap();
        } else {
            unselectNoderef();
        }
    }

    public void unselectNoderef() {
        markerSelected = null;
        markerSelectedId = -1L;
        editNodeRefPosition.setVisibility(View.GONE);
    }

    public void selectNodeRefMarker(LocationMarker<PoiNodeRef> markerSelected) {
        editNodeRefPosition.setVisibility(View.VISIBLE);
        markerSelected.setIcon(IconFactory.getInstance(getActivity()).fromResource(R.drawable.waypoint_selected));
    }

    /*-----------------------------------------------------------
    * POI CREATION
    *---------------------------------------------------------*/

    @BindView(R.id.hand)
    ImageView handImageView;

    @BindView(R.id.add_poi_few_values)
    FloatingActionsMenu addPoiFloatingButton;

    @BindView(R.id.floating_btn_wrapper)
    RelativeLayout floatingBtnWrapper;

    @BindView(R.id.pin)
    ImageButton creationPin;

    ValueAnimator valueAnimator;

    private void createPoi() {
        LatLng pos = mapboxMap.getCameraPosition().target;
        boolean needTagEdition = false;

        for (PoiTypeTag poiTypeTag : poiTypeSelected.getTags()) {
            if (StringUtils.isEmpty(poiTypeTag.getValue())) {
                needTagEdition = true;
                break;
            }
        }

        if (needTagEdition) {
            Intent intent = new Intent(getActivity(), EditPoiActivity.class);
            intent.putExtra(EditPoiActivity.CREATION_MODE, true);
            intent.putExtra(EditPoiActivity.POI_LAT, pos.getLatitude());
            intent.putExtra(EditPoiActivity.POI_LNG, pos.getLongitude());
            intent.putExtra(EditPoiActivity.POI_LEVEL, isVectorial ? currentLevel : 0d);
            intent.putExtra(EditPoiActivity.POI_TYPE, poiTypeSelected.getId());

            switchMode(MapMode.DEFAULT);
            getActivity().startActivityForResult(intent, EditPoiActivity.EDIT_POI_ACTIVITY_CODE);
        } else {
            eventBus.post(new PleaseCreateNoTagPoiEvent(poiTypeSelected, pos, isVectorial ? currentLevel : 0d));
            switchMode(MapMode.DEFAULT);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoiNoTypeCreated(PoiNoTypeCreated event) {
        presenter.setForceRefreshPoi();
        presenter.loadPoisIfNeeded();
    }

    private void animationPoiCreation() {
        handImageView.setVisibility(View.VISIBLE);
        valueAnimator = ValueAnimator.ofFloat(0, OsmCreationAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(1000);
        valueAnimator.addListener(new OsmCreationAnimatorUpdateListener(mapboxMap, handImageView, getActivity()));
        valueAnimator.addUpdateListener(new OsmCreationAnimatorUpdateListener(mapboxMap, handImageView, getActivity()));
        valueAnimator.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewPoiTypeSelected(NewPoiTypeSelected event) {
        Timber.d("Received event NewPoiTypeSelected");
        if (isTuto) {
            nextTutoStep();
        }
        poiTypeSelected(event.getPoiType());
        switchMode(MapMode.POI_CREATION);
    }

    private void poiTypeSelected(PoiType poiType) {
        poiTypeTextView.setText(poiType.getName());
        poiTypeSelected = poiType;
        savePoiTypeId = 0;
        Bitmap bitmap;

        bitmap = bitmapHandler.getMarkerBitmap(poiType, Poi.computeState(false, true, false));
        if (poiTypeHidden.contains(poiType.getId())) {
            poiTypeHidden.remove(poiType.getId());
            applyPoiFilter();
        }

        if (presenter.getNumberOfPoiTypes() > maxPoiType) {
            editPoiTypeBtn.setVisibility(View.VISIBLE);
        }

        creationPin.setImageBitmap(bitmap);
        creationPin.setVisibility(View.VISIBLE);
    }

    private void noteSelected() {
        Bitmap bitmap = bitmapHandler.getNoteBitmap(Note.computeState(null, false, true));
        creationPin.setImageBitmap(bitmap);
        creationPin.setVisibility(View.VISIBLE);
        poiTypeSelected = null;
    }

    private void showFloatingButtonAddPoi(boolean show) {
        //animation show and hide
        if (show) {
            if (floatingBtnWrapper.getVisibility() == View.GONE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                floatingBtnWrapper.startAnimation(bottomUp);
                floatingBtnWrapper.setVisibility(View.VISIBLE);
            }
        } else {
            if (floatingBtnWrapper.getVisibility() == View.VISIBLE) {
                Animation upDown = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);
                floatingBtnWrapper.startAnimation(upDown);
                floatingBtnWrapper.setVisibility(View.GONE);
            }
        }

        //which kind of add to display depending on screen size
        // if in store flavor show the spinner
        addPoiFloatingButton.setVisibility(View.VISIBLE);
        addPoiFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTuto) {
                    nextTutoStep();
                }
                easterEgg();
            }
        });
    }

    protected void loadPoiTypeFloatingBtn() {
        addPoiFloatingButton.removeAllButtons();
        FloatingActionButton floatingActionButton;

        //add note
        if (!FlavorUtils.isPoiStorage()) {
            floatingActionButton = new FloatingActionButton(getActivity());
            floatingActionButton.setTitle(getString(R.string.note));
            floatingActionButton.setColorPressed(getResources().getColor(R.color.material_green_700));
            floatingActionButton.setColorNormal(getResources().getColor(R.color.material_green_500));
            floatingActionButton.setSize(FloatingActionButton.SIZE_MINI);
            floatingActionButton.setIconDrawable(bitmapHandler.getIconWhite(null));
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchMode(MapMode.NOTE_CREATION);
                    addPoiFloatingButton.collapse();
                    if (isTuto) {
                        nextTutoStep();
                        nextTutoStep();
                    }
                }
            });
            addPoiFloatingButton.addButton(floatingActionButton);
        }

        if (presenter.getNumberOfPoiTypes() <= maxPoiType) {
            //add a btn per poiType
            for (final PoiType poiType : presenter.getPoiTypes()) {
                floatingActionButton = new FloatingActionButton(getActivity());
                floatingActionButton.setTitle(poiType.getName());
                floatingActionButton.setColorPressed(ContextCompat.getColor(getActivity(), R.color.material_blue_grey_800));
                floatingActionButton.setColorNormal(ContextCompat.getColor(getActivity(), R.color.material_blue_500));
                floatingActionButton.setSize(FloatingActionButton.SIZE_MINI);
                floatingActionButton.setIconDrawable(bitmapHandler.getIconWhite(poiType));
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (configManager.hasPoiAddition()) {
                            switchMode(MapMode.POI_CREATION);
                            poiTypeSelected(poiType);
                            addPoiFloatingButton.collapse();
                            if (isTuto) {
                                nextTutoStep();
                            }
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                addPoiFloatingButton.addButton(floatingActionButton);
            }
        } else {
            // add a btn for all poiTypes
            floatingActionButton = new FloatingActionButton(getActivity());
            floatingActionButton.setTitle(getResources().getString(R.string.add_poi));
            floatingActionButton.setColorPressed(getResources().getColor(R.color.material_blue_grey_800));
            floatingActionButton.setColorNormal(getResources().getColor(R.color.material_blue_500));
            floatingActionButton.setSize(FloatingActionButton.SIZE_MINI);
            floatingActionButton.setIconDrawable(getResources().getDrawable(R.drawable.fab_poi));
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (configManager.hasPoiAddition()) {
                        switchMode(MapMode.TYPE_PICKER);
                        if (isTuto) {
                            nextTutoStep();
                        }
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            addPoiFloatingButton.addButton(floatingActionButton);
        }

        // the floating btn and tthe poitypes are loaded we can now start the tuto
        displayTutorial(false);
    }

    protected void loadPoiTypeSpinner() {
        poiTypePickerAdapter.addAll(presenter.getPoiTypes());
        if (savePoiTypeId != 0) {
            for (PoiType poiType : presenter.getPoiTypes()) {
                if (poiType.getId().equals(savePoiTypeId)) {
                    poiTypeSelected(poiType);
                }
            }
        }
    }

    /*-----------------------------------------------------------
    * POI EDITION
    *---------------------------------------------------------*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseOpenEditionEvent(PleaseOpenEditionEvent event) {
        Timber.d("Received event PleaseOpenEdition");
        Intent intent = new Intent(getActivity(), EditPoiActivity.class);
        intent.putExtra(EditPoiActivity.CREATION_MODE, false);
        intent.putExtra(EditPoiActivity.POI_ID, ((Poi) markerSelected.getRelatedObject()).getId());
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseChangePoiPosition(PleaseChangePoiPosition event) {
        Timber.d("Received event PleaseChangePoiPosition");
        if (configManager.hasPoiModification()) {
            switchMode(MapMode.POI_POSITION_EDITION);
            creationPin.setVisibility(View.GONE);

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
            valueAnimator.setDuration(900);
            valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapboxMap.getCameraPosition().target, markerSelected.getPosition(), mapboxMap));

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    creationPin.setVisibility(View.VISIBLE);
                    Log.i(MapFragment.class.getSimpleName(), "onAnimationEnd: " + markerSelected.getRelatedObject());
                    hideMarker(markerSelected);
                }
            });
            valueAnimator.start();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
        }
    }

    /*-----------------------------------------------------------
    * POI DELETION
    *---------------------------------------------------------*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseDeletePoiFromMapEvent(PleaseDeletePoiFromMapEvent event) {
        Poi poi = (Poi) markerSelected.getRelatedObject();
        poi.setToDelete(true);
        markersPoi.remove(poi.getId());
        removeMarker(markersPoi.get(poi.getId()));
        markersPoi.remove(poi.getId());
        eventBus.post(new PleaseDeletePoiEvent(poi));
        switchMode(MapMode.DEFAULT);
    }

    /*-----------------------------------------------------------
    * POI DETAIL
    *---------------------------------------------------------*/
    private void displayPoiDetailBanner(boolean display) {
        if (display) {
            if (markerSelected != null) {
                Poi poi = (Poi) markerSelected.getRelatedObject();
                eventBus.post(new PleaseChangeValuesDetailPoiFragmentEvent(
                        poi.getType().getName(),
                        poi.getName(),
                        poi.getWay()));
            }

            if (poiDetailWrapper.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                poiDetailWrapper.startAnimation(bottomUp);
                poiDetailWrapper.setVisibility(View.VISIBLE);
            }

        } else {


            if (poiDetailWrapper.getVisibility() == View.VISIBLE) {
                Animation upDown = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);
                poiDetailWrapper.startAnimation(upDown);
                poiDetailWrapper.setVisibility(View.GONE);
            }
        }
    }

    /*-----------------------------------------------------------
    * NOTE DETAIL
    *---------------------------------------------------------*/
    private void displayNoteDetailBanner(boolean display) {
        if (display) {
            if (markerSelected != null && !markerSelected.getType().equals(LocationMarker.MarkerType.POI)) {
                eventBus.post(new PleaseChangeValuesDetailNoteFragmentEvent(
                        (Note) markerSelected.getRelatedObject()));
            }

            if (noteDetailWrapper.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                noteDetailWrapper.startAnimation(bottomUp);
                noteDetailWrapper.setVisibility(View.VISIBLE);
            }

        } else {


            if (noteDetailWrapper.getVisibility() == View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);

                noteDetailWrapper.startAnimation(bottomUp);
                noteDetailWrapper.setVisibility(View.GONE);
            }
        }
    }

    /*-----------------------------------------------------------
    * MAP UTILS
    *---------------------------------------------------------*/
    @BindView(R.id.localisation)
    FloatingActionButton floatingButtonLocalisation;

    @OnClick(R.id.localisation)
    public void setOnPosition() {
        Timber.d("Center position on user location");
        Location pos = mapboxMap.getMyLocation();
        if (pos != null) {
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(pos.getLatitude(), pos.getLongitude())).build());
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(Category.GeoLocation.getValue())
                .setAction("Center map on user geolocation")
                .build());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangeMapModeEvent(ChangeMapModeEvent event) {
        switchMode(event.getMapMode());
    }

    public LatLng getMyLocation() {
        return mapboxMap.getCameraPosition().target;
    }

    public void setZoomLevelText(String zoom) {
        zoomLevelText.setText(zoom);
    }

    public LocationMarkerOptions getMarkerOptions(LocationMarker.MarkerType markerType, Long id) {
        return markerType == LocationMarker.MarkerType.POI ? markersPoi.get(id) : markersNotes.get(id);
    }
    public LatLngBounds getViewLatLngBounds() {
        return mapboxMap.getProjection().getVisibleRegion().latLngBounds;
    }

    public float getZoomLevel() {
        return (float) mapboxMap.getCameraPosition().zoom;
    }

    public void changeMapZoomSmooth(final double zoom) {
        mapboxMap.easeCamera(new CameraUpdate() {
            @Override
            public CameraPosition getCameraPosition(@NonNull MapboxMap mapboxMap) {
                return new CameraPosition.Builder().target(mapboxMap.getCameraPosition().target).zoom(zoom).build();
            }
        }, 700);
    }

    public void changeMapPositionSmooth(final LatLng newPosition) {
        mapboxMap.easeCamera(new CameraUpdate() {
            @Override
            public CameraPosition getCameraPosition(@NonNull MapboxMap mapboxMap) {
                return new CameraPosition.Builder().target(newPosition).build();
            }
        }, 700);
    }

    public void hideMarker(Marker marker) {
        marker.setIcon(IconFactory.getInstance(getActivity()).fromResource(R.drawable.hidden_marker));
    }

    /*-----------------------------------------------------------
    * EASTER EGG
    *---------------------------------------------------------*/
    Long previousTime = System.currentTimeMillis();
    int nbClick = 0;


    private void easterEgg() {

        Long time = System.currentTimeMillis();

        if (time - previousTime < 500) {
            if (++nbClick == 4) {
                eventBus.post(new PleaseShowMeArpiglEvent());
                eventBus.post(new PleaseToggleArpiEvent());
                Toast.makeText(getActivity(), getString(R.string.easter_egg_activation), Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putBoolean(getString(R.string.easter_egg), true).apply();
                nbClick = 0;
            }
        } else {
            nbClick = 0;
        }

        previousTime = time;

    }


    /*-----------------------------------------------------------
    * SYNC
    *---------------------------------------------------------*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncFinishUploadPoiEvent(SyncFinishUploadPoiEvent event) {
        boolean forceRefresh = false;

        if (event.getSuccessfullyAddedPoisCount() > 0) {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.add_done), event.getSuccessfullyAddedPoisCount()), Toast.LENGTH_SHORT).show();
            forceRefresh = true;
        }
        if (event.getSuccessfullyUpdatedPoisCount() > 0) {
            Toast.makeText(getActivity(), String.format(getResources().getString(mapMode == MapMode.WAY_EDITION ? R.string.noderef_moved : R.string.update_done), event.getSuccessfullyUpdatedPoisCount()), Toast.LENGTH_SHORT).show();
            forceRefresh = true;
        }
        if (event.getSuccessfullyDeletedPoisCount() > 0) {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.delete_done), event.getSuccessfullyDeletedPoisCount()), Toast.LENGTH_SHORT).show();
            forceRefresh = true;
        }

        if (forceRefresh) {
            presenter.setForceRefreshPoi();
            presenter.loadPoisIfNeeded();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewNoteCreatedEvent(NewNoteCreatedEvent event) {
        // a note has been created, select it
        markerSelectedId = event.getNoteId();
        markerSelected = null;
        selectedMarkerType = LocationMarker.MarkerType.NOTE;
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApplyNewCommentFailedEvent(ApplyNewCommentFailedEvent event) {
        Toast.makeText(getActivity(), getString(R.string.failed_apply_comment), Toast.LENGTH_SHORT).show();
        markerSelectedId = null;
        markerSelected = null;
        selectedMarkerType = LocationMarker.MarkerType.NONE;
        switchMode(MapMode.DEFAULT);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUnauthorizedEvent(SyncUnauthorizedEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_connect_retrofit, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncDownloadRetrofitErrorEvent(SyncDownloadRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_download_retrofit, Toast.LENGTH_SHORT).show();
        showProgressBar(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncConflictingNodeErrorEvent(SyncConflictingNodeErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_update_node, Toast.LENGTH_LONG).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncNewNodeErrorEvent(SyncNewNodeErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_create_node, Toast.LENGTH_LONG).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUploadRetrofitErrorEvent(SyncUploadRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_upload_retrofit, Toast.LENGTH_SHORT).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUploadNoteRetrofitErrorEvent(SyncUploadNoteRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_upload_retrofit, Toast.LENGTH_SHORT).show();
        removeNoteMarkerInError(event.getNoteIdInError());
    }

    // markers were added on the map the sync failed we remove them
    private void removePoiMarkerInError(Long id) {
        Marker m = markersPoi.remove(id).getMarker();
        if (m != null) {
            mapboxMap.removeMarker(m);
        }
        defaultMap();
    }

    // note was added on the map the sync failed we remove it
    private void removeNoteMarkerInError(Long id) {
        Marker m = markersNotes.remove(id).getMarker();
        if (m != null) {
            mapboxMap.removeMarker(m);
        }
        defaultMap();
    }

    /*-----------------------------------------------------------
    * VECTORIAL
    *---------------------------------------------------------*/

    @BindView(R.id.level_bar)
    LevelBar levelBar;

    //private VectorialOverlay vectorialOverlay;
    private boolean isVectorial = false;
    private double currentLevel = 0;
    Set<VectorialObject> vectorialObjectsBackground = new HashSet<>();
    private LocationMarker.MarkerType selectedMarkerType = LocationMarker.MarkerType.NONE;
    private int zoomVectorial;
    private Set<Way> waysEdition = new HashSet<>();


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEditionWaysLoadedEvent(EditionWaysLoadedEvent event) {
        if (event.isRefreshFromOverpass()) {
            progressBar.setVisibility(View.GONE);
        }
        if (mapMode == MapMode.WAY_EDITION) {
            Timber.d("Showing nodesRefs : " + event.getWays().size());
            clearAllNodeRef();
            waysEdition.clear();
            waysEdition.addAll(event.getWays());
            updateVectorial(event.getLevels());

//            if (getMarkerSelected() != null && markerSelected.getType().equals(LocationMarker.MarkerType.NODE_REF)) {
//                boolean reselectMaker = false;
//                for (VectorialObject v : vectorialObjectsEdition) {
//                    if (v.getId().equals(((PoiNodeRef) markerSelected.getRelatedObject()).getNodeBackendId())) {
//                        reselectMaker = true;
//                    }
//                }
//                if (!reselectMaker) {
//                    unselectNoderef();
//                }
//            }
        }
    }

    private void updateVectorial(TreeSet<Double> levels) {
        for (Way way: waysEdition) {
            mapboxMap.addPolyline(way.getPolylineOptions());
            for (PoiNodeRef poiNodeRef : way.getPoiNodeRefs()) {
                LocationMarkerOptions<PoiNodeRef> markerOptions =
                        new LocationMarkerOptions<PoiNodeRef>().position(poiNodeRef.getPosition())
                                .icon(IconFactory.getInstance(getActivity()).fromResource(R.drawable.waypoint))
                                .relatedObject(poiNodeRef);
                mapboxMap.addMarker(markerOptions);
            }
        }
//        if (vectorialOverlay == null) {
//            Set<VectorialObject> vectorialObjects = new HashSet<>();
//            vectorialObjects.addAll(vectorialObjectsBackground);
//            vectorialObjects.addAll(vectorialObjectsEdition);
//
//            vectorialOverlay = new VectorialOverlay(zoomVectorial, vectorialObjects, levels, getResources().getDisplayMetrics().scaledDensity);
//            /** TODO **/
//            //mapView.addOverlay(vectorialOverlay);
//        } else {
//            Set<VectorialObject> vectorialObjects = new HashSet<>();
//            vectorialObjects.addAll(vectorialObjectsBackground);
//            vectorialObjects.addAll(vectorialObjectsEdition);
//
//            vectorialOverlay.setVectorialObjects(vectorialObjects);
//            vectorialOverlay.setLevels(levels);
//        }
//        invalidateMap();
//
//        levelBar.setLevels(vectorialOverlay.getLevels(), currentLevel);
//        if (levelBar.getLevels().length < 2) {
//            levelBar.setVisibility(View.INVISIBLE);
//        } else {
//            levelBar.setVisibility(View.VISIBLE);
//        }
    }

    private void clearVectorialEdition() {
//        if (vectorialOverlay == null) {
//            return;
//        } else {
//            vectorialObjectsEdition.clear();
//            Set<VectorialObject> vectorialObjects = new HashSet<>();
//            vectorialObjects.addAll(vectorialObjectsBackground);
//            vectorialOverlay.setVectorialObjects(vectorialObjects);
//            invalidateMap();
//        }
//        levelBar.setLevels(vectorialOverlay.getLevels(), currentLevel);
//        if (levelBar.getLevels().length < 2) {
//            levelBar.setVisibility(View.INVISIBLE);
//        } else {
//            levelBar.setVisibility(View.VISIBLE);
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTooManyRequestsEvent(TooManyRequestsEvent event) {
        progressBar.setVisibility(View.GONE);
    }


    /*-----------------------------------------------------------
    * PoiType picker
    *---------------------------------------------------------*/

    @BindView(R.id.poi_type_value)
    EditText poiTypeEditText;

    @BindView(R.id.poi_type)
    TextView poiTypeTextView;

    @BindView(R.id.edit_poi_type)
    ImageButton editPoiTypeBtn;

    @BindView(R.id.autocomplete_list)
    ListView poiTypeListView;

    @BindView(R.id.poi_type_value_wrapper)
    RelativeLayout poiTypeHeaderWrapper;

    @OnClick(R.id.edit_poi_type)
    public void onEditPoiTypeWrapperClick() {
        editPoiType();
    }

    @OnClick(R.id.poi_type_value_wrapper)
    public void onEditPoiTypeClick() {
        editPoiType();
    }

    public void editPoiType() {
        if (mapMode == MapMode.POI_CREATION) {
            switchMode(MapMode.TYPE_PICKER);
        }
    }

    private long savePoiTypeId = 0;
    private PoiTypePickerAdapter poiTypePickerAdapter;
    private List<PoiType> autocompletePoiTypeValues = new ArrayList<>();

    private void instantiatePoiTypePicker() {
        poiTypePickerAdapter = new PoiTypePickerAdapter(getActivity(), autocompletePoiTypeValues, poiTypeEditText, eventBus, bitmapHandler, sharedPreferences.getBoolean(getString(R.string.shared_prefs_expert_mode), false));
        poiTypeListView.setAdapter(poiTypePickerAdapter);

        // Add Text Change Listener to EditText
        poiTypeEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                poiTypePickerAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void displayPoiTypePicker() {
        if (mapMode == MapMode.TYPE_PICKER) {
            if (poiTypeListView.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_poi_type);

                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    bottomUp.setInterpolator(getActivity(), android.R.interpolator.linear_out_slow_in);
                }

                //the text view will be changed to edit text at the end of the animation
                bottomUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        poiTypeEditText.setVisibility(View.VISIBLE);
                        poiTypeTextView.setVisibility(View.GONE);
                        editPoiTypeBtn.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                poiTypeListView.startAnimation(bottomUp);
                poiTypeListView.setVisibility(View.VISIBLE);

                // animation of the edit text from the top
                if (poiTypeTextView.getVisibility() == View.GONE) {
                    Animation slideTop = AnimationUtils.loadAnimation(getActivity(),
                            R.anim.slide_top_annimation);
                    poiTypeHeaderWrapper.startAnimation(slideTop);
                    poiTypeHeaderWrapper.setVisibility(View.VISIBLE);
                    poiTypeEditText.setVisibility(View.VISIBLE);
                }
            }
        } else if (mapMode == MapMode.POI_CREATION) {
            if (poiTypeListView.getVisibility() == View.VISIBLE) {
                Animation upBottom = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_poi_type);
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    upBottom.setInterpolator(getActivity(), android.R.interpolator.linear_out_slow_in);
                }
                poiTypeListView.startAnimation(upBottom);
            }
            poiTypeListView.setVisibility(View.GONE);
            poiTypeEditText.setVisibility(View.GONE);
            // if we have few value we hide the editPoiType btn
            if (presenter.getNumberOfPoiTypes() > maxPoiType) {
                editPoiTypeBtn.setVisibility(View.VISIBLE);
            }
            poiTypeTextView.setVisibility(View.VISIBLE);
        } else if (mapMode == MapMode.NOTE_CREATION) {
            poiTypeHeaderWrapper.setVisibility(View.VISIBLE);
            poiTypeTextView.setVisibility(View.VISIBLE);
            poiTypeListView.setVisibility(View.GONE);
            poiTypeEditText.setVisibility(View.GONE);
            editPoiTypeBtn.setVisibility(View.GONE);
            poiTypeTextView.setText(getResources().getString(R.string.note));
        } else {
            //For default behavior hide all poitype picker components
            poiTypeListView.setVisibility(View.GONE);
            poiTypeEditText.setVisibility(View.GONE);
            poiTypeTextView.setVisibility(View.GONE);
            editPoiTypeBtn.setVisibility(View.GONE);
            poiTypeHeaderWrapper.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLastUsePoiTypeLoaded(LastUsePoiTypeLoaded event) {
        poiTypePickerAdapter.addAllLastUse(event.getAutocompleteLastUsePoiTypeValues());
        poiTypeListView.setSelectionAfterHeaderView();
    }

    /*-----------------------------------------------------------
    * ADDRESS
    *---------------------------------------------------------*/
    @BindView(R.id.addressView)
    TextView addressView;

    @Inject
    Geocoder geocoder;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAddressFoundEvent(AddressFoundEvent event) {
        Timber.d("Received event AddressFound");
        if (getZoomLevel() >= zoomVectorial) {
            addressView.setVisibility(View.VISIBLE);
        }
        addressView.setText(event.getAddress());
    }

    /*-----------------------------------------------------------
    * FILTERS
    *---------------------------------------------------------*/

    private List<Long> poiTypeHidden = new ArrayList<>();

    private boolean displayOpenNotes = true;
    private boolean displayClosedNotes = true;

    public List<Long> getPoiTypeHidden() {
        return poiTypeHidden;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseApplyPoiFilter(PleaseApplyPoiFilter event) {
        Timber.d("filtering Pois by type");
        poiTypeHidden = event.getPoiTypeIdsToHide();
        applyPoiFilter();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseApplyNoteFilterEvent(PleaseApplyNoteFilterEvent event) {
        Timber.d("filtering Notes");
        displayOpenNotes = event.isDisplayOpenNotes();
        displayClosedNotes = event.isDisplayClosedNotes();
        applyNoteFilter();
    }

    public void applyNoteFilter() {
        for (LocationMarkerOptions marker : markersNotes.values()) {
            removeMarker(marker);
            addNoteMarkerDependingOnFilters(marker);
        }
    }

    public void applyPoiFilter() {
        for (LocationMarkerOptions marker : markersPoi.values()) {
            removeMarker(marker);
            addPoiMarkerDependingOnFilters(marker);
        }
    }

    private void addNoteMarkerDependingOnFilters(LocationMarkerOptions<Note> marker) {
        Note note = marker.getMarker().getRelatedObject();

        if ((displayOpenNotes && Note.STATUS_OPEN.equals(note.getStatus())) || Note.STATUS_SYNC.equals(note.getStatus()) || (displayClosedNotes && Note.STATUS_CLOSE.equals(note.getStatus()))) {
            mapboxMap.addMarker(marker);
        } else if (mapMode.equals(MapMode.DETAIL_NOTE) && ((Note) markerSelected.getRelatedObject()).getId().equals(note.getId())) {
            switchMode(MapMode.DEFAULT);
        }
    }

    private void addPoiMarkerDependingOnFilters(LocationMarkerOptions<Poi> markerOption) {
        Poi poi = markerOption.getMarker().getRelatedObject();
        //if we are in vectorial mode we hide all poi not at the current level
        if (poi.getType() != null && !poiTypeHidden.contains(poi.getType().getId()) && (!isVectorial || poi.isAtLevel(currentLevel) || !poi.isOnLevels(levelBar.getLevels()))) {
            mapboxMap.addMarker(markerOption);
        } else if (mapMode.equals(MapMode.DETAIL_POI) && ((Poi) markerSelected.getRelatedObject()).getId().equals(poi.getId())) {
            //if the poi selected is hidden close the detail mode
            switchMode(MapMode.DEFAULT);
        }
    }

    /*-----------------------------------------------------------
    * TUTORIAL
    *---------------------------------------------------------*/
    public static final String TUTORIAL_CREATION_FINISH = "TUTORIAL_CREATION_FINISH";
    private ShowcaseView showcaseView;
    private int showcaseCounter = 0;
    private boolean isTuto = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPleaseDisplayTutorialEvent(PleaseDisplayTutorialEvent event) {
        switchMode(MapMode.DEFAULT);
        displayTutorial(true);
    }

    protected void displayTutorial(boolean forceDisplay) {
        showcaseCounter = 0;

        if (presenter.getNumberOfPoiTypes() < 1) {
            return;
        }

        if (!configManager.hasPoiAddition()) {
            sharedPreferences.edit().putBoolean(TUTORIAL_CREATION_FINISH, true).apply();
            return;
        }

        boolean showTuto = forceDisplay || !sharedPreferences.getBoolean(TUTORIAL_CREATION_FINISH, false);

        if (showTuto && !isTuto) {
            isTuto = true;
            //position OK button on the left
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.setMargins(60, 0, 0, 200);

            showcaseView = new ShowcaseView.Builder(getActivity(), true)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(getString(R.string.tuto_title_press_create))
                    .setContentText(getString(R.string.tuto_text_press_create))
                    .setTarget(new ViewTarget(addPoiFloatingButton.getmAddButton()))
                    .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                switch (showcaseCounter) {
                                                    case 0:
                                                        addPoiFloatingButton.expand();
                                                        nextTutoStep();
                                                        break;

                                                    case 1:
                                                        addPoiFloatingButton.getChildAt(1).performClick();
                                                        break;

                                                    case 2:
                                                        //chose the first type
                                                        poiTypeSelected(presenter.getPoiTypes().iterator().next());
                                                        switchMode(MapMode.POI_CREATION);
                                                        nextTutoStep();
                                                        break;

                                                    case 3:
                                                        nextTutoStep();
                                                        break;

                                                    case 4:
                                                        nextTutoStep();
                                                        switchMode(MapMode.DEFAULT);
                                                        break;
                                                }
                                            }
                                        }
                    )
                    .build();

            showcaseView.setButtonPosition(params);
        }
    }

    private void nextTutoStep() {
        switch (showcaseCounter) {
            case 0:
                if (presenter.getNumberOfPoiTypes() <= maxPoiType) {
                    showcaseView.setContentText(getString(R.string.tuto_text_choose_type));
                    showcaseCounter++;
                } else {
                    //compact view
                    showcaseView.setContentText(getString(R.string.tuto_text_poi_or_note));
                }
                showcaseView.setTarget(new ViewTarget(addPoiFloatingButton.getChildAt(0)));
                break;

            case 1:
                showcaseView.setContentText(getString(R.string.tuto_text_choose_type));
                showcaseView.setTarget(new ViewTarget(addPoiFloatingButton.getChildAt(0)));
                break;

            case 2:
                showcaseView.setContentText(getString(R.string.tuto_text_swipe_for_position));
                showcaseView.setTarget(new ViewTarget(mapView));
                break;

            case 3:
                showcaseView.setContentText(getString(R.string.tuto_text_confirm_position_creation));
                showcaseView.setTarget(new ViewTarget(R.id.action_confirm_position, getActivity()));
                break;

            case 4:
                closeTuto();
                break;
        }

        showcaseCounter++;
    }

    private void closeTuto() {
        showcaseView.hide();
        isTuto = false;
        sharedPreferences.edit().putBoolean(TUTORIAL_CREATION_FINISH, true).apply();
    }

    public FloatingActionsMenu getAddPoiFloatingButton() {
        return addPoiFloatingButton;
    }

/*
    *//*-----------------------------------------------------------
    * TILE SOURCES
    *---------------------------------------------------------*//*
    private static final String TILE_SOURCE = "TILE_SOURCE";
    private static final String OSM_MBTILES_FILE = "osm.mbtiles";
    private static final String BING_MBTILES_FILE = "satellite.mbtiles";

    private TileLayer osmTileLayer;
    private TileLayer bingTileLayer;

    // Ids of tile layers
    public static final String OSM_TILE_SOURCE = "OSM_TILE_SOURCE";
    public static final String BING_TILE_SOURCE = "BING_TILE_SOURCE";
    private String currentTileLayer;

    private static final float MIN_ZOOM_LEVEL = 5;

    private BoundingBox scrollableAreaLimit = null;

    *//**
     * Switch the tile source between the osm tile source and the bing aerial vue tile source.
     *//*
    private void switchTileSource() {
        switchToTileSource(BING_TILE_SOURCE.equals(currentTileLayer) ? OSM_TILE_SOURCE : BING_TILE_SOURCE);
    }

    *//**
     * Change the tile source of the mapView.
     *
     * @param tileSourceId The id of the new tile source.
     *//*
    private void switchToTileSource(String tileSourceId) {
        Timber.d("Switch tileSource to %s", tileSourceId);
        switch (tileSourceId) {
            case BING_TILE_SOURCE:
                currentTileLayer = BING_TILE_SOURCE;
                mapView.setTileSource(bingTileLayer);
                break;
            case OSM_TILE_SOURCE:
            default:
                currentTileLayer = OSM_TILE_SOURCE;
                mapView.setTileSource(osmTileLayer);
                break;
        }
        // Reset the scroll limit of the map
        mapView.setScrollableAreaLimit(scrollableAreaLimit);
    }

    *//**
     * Instantiate the different tiles sources used by the map.
     *//*
    private void instantiateTileSources() {
        if (FlavorUtils.isTemplate()) {
            try {
                List<String> assetsList = Arrays.asList(getActivity().getResources().getAssets().list(""));
                if (assetsList.contains(OSM_MBTILES_FILE)) {
                    // Create a TileSource with OpenstreetMap's Tiles from a MBTiles file
                    osmTileLayer = new MBTilesLayer(getActivity(), OSM_MBTILES_FILE, configManager.getZoomMaxProvider())
                            .setName("OpenStreetMap")
                            .setAttribution("© OpenStreetMap Contributors")
                            .setMaximumZoomLevel(configManager.getZoomMax());
                }

                if (assetsList.contains(BING_MBTILES_FILE)) {
                    // Create a TileSource with Bing Maps' Tiles from a MBTiles file
                    bingTileLayer = new MBTilesLayer(getActivity(), BING_MBTILES_FILE, configManager.getZoomMaxProvider())
                            .setName("Bing aerial view")
                            .setMaximumZoomLevel(configManager.getZoomMax());
                }
            } catch (IOException e) {
                Timber.e(e, "Couldn't get assets list");
            }
        }

        if (osmTileLayer == null) {
            // Create a TileSource from OpenStreetMap server
            osmTileLayer = new WebSourceTileLayer("openstreetmap", configManager.getMapUrl(), configManager.getZoomMaxProvider())
                    .setName("OpenStreetMap")
                    .setAttribution("© OpenStreetMap Contributors")
                    .setMinimumZoomLevel(MIN_ZOOM_LEVEL)
                    .setMaximumZoomLevel(configManager.getZoomMax());
        }
        if (bingTileLayer == null) {
            // Create a TileSource from Bing map with aerial with label style
            bingTileLayer = new BingTileLayer(configManager.getBingApiKey(), BingTileLayer.IMAGERYSET_AERIALWITHLABELS, configManager.getZoomMaxProvider())
                    .setName("Bing aerial view")
                    .setMinimumZoomLevel(MIN_ZOOM_LEVEL)
                    .setMaximumZoomLevel(configManager.getZoomMax());
        }

        // Set the map bounds
        if (configManager.hasBounds()) {
            scrollableAreaLimit = configManager.getLatLngBounds();
        }

        // Set the map bounds for the map
        if (FlavorUtils.isTemplate()) {
            scrollableAreaLimit = Box.enlarge(configManager.getLatLngBounds(), 2);
            drawBounds();
        }
    }*/

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onPleaseSwitchMapStyleEvent(PleaseSwitchMapStyleEvent event) {
//        switchTileSource();
//    }

    /*-----------------------------------------------------------
    * GETTERS AND SETTERS
    *---------------------------------------------------------*/

    public int getZoomVectorial() {
        return zoomVectorial;
    }

    public boolean isTuto() {
        return isTuto;
    }

    public MapMode getMapMode() {
        return mapMode;
    }

    public LocationMarker getMarkerSelected() {
        return markerSelected;
    }

    public void setMarkerSelected(LocationMarker markerSelected) {
        selectedMarkerType = markerSelected.getType();
        this.markerSelected = markerSelected;
    }

    public Long getMarkerSelectedId() {
        return markerSelectedId;
    }

    public void setMarkerSelectedId(Long markerSelectedId) {
        this.markerSelectedId = markerSelectedId;
    }

    public LocationMarker.MarkerType getSelectedMarkerType() {
        return selectedMarkerType;
    }

    public BitmapHandler getBitmapHandler() {
        return bitmapHandler;
    }

    public LevelBar getLevelBar() {
        return levelBar;
    }

    public TextView getAddressView() {
        return addressView;
    }

    public boolean isVectorial() {
        return isVectorial;
    }

    public void setVectorial(boolean vectorial) {
        isVectorial = vectorial;
    }

    public Geocoder getGeocoder() {
        return geocoder;
    }

    public MapFragmentPresenter getPresenter() {
        return presenter;
    }

}