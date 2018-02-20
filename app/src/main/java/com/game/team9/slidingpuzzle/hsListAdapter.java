/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.database.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gongb on 2/12/2018.
 */

public class hsListAdapter extends ArrayAdapter<User> {
    private int mResource;

    public hsListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        mResource = resource;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User highscore = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
        TextView tvIndex = (TextView) view.findViewById(R.id.index);
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvScore = (TextView)view.findViewById(R.id.score);
        tvIndex.setText(String.valueOf(position+1));
        tvName.setText(highscore.getName());
        tvScore.setText(String.valueOf(highscore.getScore()));

        return view;
    }

}
