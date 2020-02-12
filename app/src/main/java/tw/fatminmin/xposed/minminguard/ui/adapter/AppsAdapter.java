package tw.fatminmin.xposed.minminguard.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import tw.fatminmin.xposed.minminguard.Common;
import tw.fatminmin.xposed.minminguard.R;
import tw.fatminmin.xposed.minminguard.ui.UIUtils;
import tw.fatminmin.xposed.minminguard.ui.dialog.AppDetailDialogFragment;
import tw.fatminmin.xposed.minminguard.ui.fragments.MainFragment;
import tw.fatminmin.xposed.minminguard.ui.models.AppDetails;

import java.util.ArrayList;

/**
 * Created by fatminmin on 2015/10/1.
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder>
{

    private final Context mContext;
    private final SharedPreferences mPref;
    private ArrayList<AppDetails> appList;
    private MainFragment.FragmentMode mMode;

    public AppsAdapter(Context context, ArrayList<AppDetails> list, MainFragment.FragmentMode mode)
    {
        mContext = context;
        appList = list;

        Context ctx = ContextCompat.createDeviceProtectedStorageContext(context);
        if (ctx == null) ctx = context;

        mPref = ctx.getSharedPreferences(Common.MOD_PREFS, Context.MODE_PRIVATE);
        mMode = mode;
    }

    public void setAppList(ArrayList<AppDetails> list)
    {
        appList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_app, parent, false);
        return new ViewHolder(v);
    }

    private void setState(String pkgName, boolean checked)
    {
        /* auto mode */
        if (mMode == MainFragment.FragmentMode.AUTO)
        {
            return;
        }

        if (mMode == MainFragment.FragmentMode.BLACKLIST)
        {
            /* blacklist mode */
            mPref.edit().putBoolean(pkgName, checked).apply();
        }
        else
        {
            /* whitelist mode */
            mPref.edit().putBoolean(Common.getWhiteListKey(pkgName), checked).apply();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        final AppDetails currentAppDetails = appList.get(position);

        holder.txtBlockNum.setText("");
        holder.imgAppIcon.setImageDrawable(currentAppDetails.getIcon());
        holder.txtAppName.setText(currentAppDetails.getName());

        if (mMode == MainFragment.FragmentMode.AUTO)
        {
            holder.switchEnable.setVisibility(View.GONE);
        }
        else
        {
            holder.switchEnable.setVisibility(View.VISIBLE);
        }

        holder.switchEnable.setChecked(currentAppDetails.isEnabled());
        holder.switchEnable.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Switch sw = (Switch) v;
                setState(currentAppDetails.getPackageName(), sw.isChecked());
            }
        });

        holder.card.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFragment dialog = AppDetailDialogFragment.newInstance(currentAppDetails.getName(), currentAppDetails.getPackageName());
                AppCompatActivity activity = (AppCompatActivity) mContext;
                dialog.show(activity.getSupportFragmentManager(), "dialog");
            }
        });

        holder.imgAppIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UIUtils.restartApp(mContext, currentAppDetails.getPackageName());
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        View card;
        ImageView imgAppIcon;
        TextView txtAppName;
        TextView txtBlockNum;
        Switch switchEnable;

        ViewHolder(View v)
        {
            super(v);

            card = v;
            imgAppIcon = v.findViewById(R.id.img_app_icon);
            txtAppName = v.findViewById(R.id.txt_app_name);
            txtBlockNum = v.findViewById(R.id.txt_block_num);
            switchEnable = v.findViewById(R.id.switch_enable);
        }
    }
}
