package il.androidcourse.infonow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class NewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RSSItemAdapter adapter;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        int itemSize = sharedPreferences.getInt("item_size", 80); // Default size 80

        adapter = new RSSItemAdapter(itemSize);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public RSSItemAdapter getAdapter() {
        return adapter;
    }

    public void setRSSItems(List<RSSItem> items) {
        adapter.setRSSItems(items);
    }

    public void addRSSItems(List<RSSItem> items) {
        adapter.addRSSItems(items);
    }
}
