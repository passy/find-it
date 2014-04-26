package com.adrianavecchioli.findit;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.adrianavecchioli.findit.adapter.ScrollAdapter;
import com.adrianavecchioli.findit.db.SqlHelper;
import com.adrianavecchioli.findit.domain.RememberItem;
import com.adrianavecchioli.findit.util.RememberUtils;
import com.google.android.glass.app.Card;
import com.google.android.glass.app.Card.ImageLayout;
import com.google.android.glass.widget.CardScrollView;

public class Find extends Activity implements Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> voiceResults = getIntent().getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        String tag= voiceResults.get(0);
        RememberItem item=SqlHelper.getInstance(this).findRememberItem(tag);
        if(item!=null){
        	List<RememberItem> items=new ArrayList<RememberItem>();
        	items.add(item);
        	displayRememberItems(items);
        } else{
        	if(RememberUtils.EVERY_THING.equalsIgnoreCase(tag)){
            	List<RememberItem> items=SqlHelper.getInstance(this).findAllRememberItem();
            	displayRememberItems(items);
            }
        	else{
        		displayFailureView();
        	}
        }
    }

	private void displayFailureView() {
		Card fail = new Card(this);
		fail.setText(R.string.storefailhead);
		fail.setFootnote(R.string.storefailfoot);
		fail.setImageLayout(Card.ImageLayout.FULL);
		fail.addImage(R.drawable.storefailbackground);
		View failView = fail.getView();
		setContentView(failView);
	}
	private Card createCardOfRememberItem(RememberItem item) {
		Card card = new Card(this);
		card.setText(item.getTag());
		card.setFootnote(String.format("%tc", item.getAddedDate()));
		card.setImageLayout(ImageLayout.FULL);
		card.addImage(BitmapFactory.decodeFile(item.getImagePath()));
		return card;
	}
	private void launchGoogleMap(RememberItem item) {
		Location location=item.getLocation();
		Intent intent=RememberUtils.getGeoIntentFromLocation(location);
		Find.this.startActivity(intent);
	}
	private void displayRememberItems(final List<RememberItem> items) {
		List<Card> mCards = new ArrayList<Card>();
		 for(RememberItem item: items){
			 Card card=createCardOfRememberItem(item);
			 mCards.add(card);
		 }
		CardScrollView mCardScrollView = new CardScrollView(this);
		final ScrollAdapter adapter = new ScrollAdapter(mCards);
		mCardScrollView.setOnItemClickListener(new OnItemClickListener() {
			
						@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Card card=(Card)adapter.getItem(arg2);
				RememberItem item=SqlHelper.getInstance(getApplicationContext()).findRememberItem(card.getText().toString());
				openOptionsMenu();
				//launchGoogleMap(item);
				
			}
		});
		
		mCardScrollView.setAdapter(adapter);
		mCardScrollView.activate();
		setContentView(mCardScrollView);
	}

	private void showSucessDeleteCard(RememberItem item){
		Card card = new Card(this);
		card.setText(R.string.object_delete);
		card.setImageLayout(ImageLayout.FULL);
		card.addImage(R.drawable.finditlogobg);
		setContentView(card.getView());
		Handler handler=new Handler(this);
		handler.sendEmptyMessageDelayed(0, 3000);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		finish();
		return false;
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.saveditemmenu, menu);
	    return true;
	}
		
	public boolean onOptionsItemSelected(MenuItem menuitem, RememberItem item) {
	    switch (menuitem.getItemId()) {
	        case R.id.menu_getdirections:
	        	launchGoogleMap(item);
	            return true;
	        case R.id.menu_delete:
	            SqlHelper.getInstance(getApplication()).deleteRememberItem(item);
				showSucessDeleteCard(item);
	            return true;
	        default:
	            return super.onOptionsItemSelected(menuitem);
	    }
	}
	
	
	
//	   @Override
//	    public boolean onKeyDown(int keyCode, KeyEvent event) {
//	          if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
//	              openOptionsMenu();
//	              return true;
//	          }
//	          return false;
//	    }
//	
//    @Override
//    public void onOptionsMenuClosed(Menu menu) {
//        finish();
//    }
}
 