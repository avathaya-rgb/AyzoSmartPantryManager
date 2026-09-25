package com.example.ayzossmartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ayzossmartpantrymanager.R;
import com.example.ayzossmartpantrymanager.model.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public interface  Listener {
        void onRecipeClicked(Recipe recipe);
    }
    private final List<Recipe> recipes;
    private final Listener listener;
    private final List<String> subtitles;

    public RecipeAdapter(List<Recipe> recipes, Listener listener) {
        this(recipes, listener, null);
    }
    public RecipeAdapter(List<Recipe> recipes, Listener listener, List<String> subtitles) {
        this.recipes = recipes;
        this.listener = listener;
        this.subtitles = subtitles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.ingredientCount.setText(recipe.getIngredients().size() + "ingredients");

        if (subtitles != null && position <subtitles.size() && subtitles.get(position) != null) {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(subtitles.get(position));
        } else {
            holder.subtitle.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, ingredientCount, subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textRecipeName);
            ingredientCount = itemView.findViewById(R.id.textIngredientCount);
            subtitle = itemView.findViewById(R.id.textRecipeSubtitle);
        }
    }
}
