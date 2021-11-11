package me.zed.elementhistorydialog;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

public class TableUtil {

    public static TableRow addTagTableHeading(Context ctx) {
        TableRow tr = new TableRow(ctx);
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        TextView tv1 = new TextView(ctx);
        TextView tv2 = new TextView(ctx);
        TextView tv3 = new TextView(ctx);

        tv1.setText(R.string.zed_ehd_key_text);
        tv2.setText(ctx.getString(R.string.zed_ehd_version_a));
        tv3.setText(ctx.getString(R.string.zed_ehd_version_b));

        tv1.setTypeface(null, Typeface.BOLD);
        tv2.setTypeface(null, Typeface.BOLD);
        tv3.setTypeface(null, Typeface.BOLD);
        tv1.setGravity(Gravity.CENTER_HORIZONTAL);
        tv2.setGravity(Gravity.CENTER_HORIZONTAL);
        tv3.setGravity(Gravity.CENTER_HORIZONTAL);

        tr.addView(tv1);
        tr.addView(tv2);
        tr.addView(tv3);

        return tr;
    }

    public static TableRow addEmptyRow(Context ctx) {
        TableRow tr = new TableRow(ctx);
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        TextView tv1 = new TextView(ctx);
        TextView tv2 = new TextView(ctx);
        TextView tv3 = new TextView(ctx);
        tv1.setText("-");
        tv2.setText("-");
        tv3.setText("-");
        tr.addView(tv1);
        tr.addView(tv2);
        tr.addView(tv3);
        return tr;
    }


    public static TableRow addTableRow(String keyValue, String aValue, String bValue, Context ctx) {

        TableRow tr = new TableRow(ctx);
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        tr.addView(getTextViewForTable(12, keyValue, ctx));
        tr.addView(getTextViewForTable(12, aValue, ctx));
        tr.addView(getTextViewForTable(12, bValue, ctx));

        return tr;
    }

    public static TextView getTextViewForTable(int ems, String text, Context ctx) {
        TextView tv = new TextView(ctx);
        tv.setMaxEms(ems);
        tv.setSingleLine(true);
        tv.setHorizontallyScrolling(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setOnLongClickListener(v -> {
            Util.makeToast(ctx, text);
            return false;
        });
        tv.setText(text);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setPadding(12, 0, 12, 0);
        return tv;
    }

    public static TableRow getCustomTableRow(List<String> headings, Context ctx) {
        TableRow tr = new TableRow(ctx);
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        for (String heading : headings) {
            TextView tv1 = new TextView(ctx);
            tv1.setText(heading);
            tv1.setTypeface(null, Typeface.BOLD);
            tr.addView(tv1);
            tv1.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        return tr;
    }

}
