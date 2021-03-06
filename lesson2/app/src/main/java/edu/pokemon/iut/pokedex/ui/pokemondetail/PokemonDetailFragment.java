package edu.pokemon.iut.pokedex.ui.pokemondetail;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dagger.Binds;
import edu.pokemon.iut.pokedex.PokedexApp;
import edu.pokemon.iut.pokedex.R;
import edu.pokemon.iut.pokedex.architecture.BaseFragment;
import edu.pokemon.iut.pokedex.architecture.NavigationManager;
import edu.pokemon.iut.pokedex.architecture.listener.PokemonGestureListener;
import edu.pokemon.iut.pokedex.data.model.Pokemon;
import edu.pokemon.iut.pokedex.data.model.Type;

/**
 * Fragment to show Unique Pokemon
 */
@SuppressWarnings("WeakerAccess")
public class PokemonDetailFragment extends BaseFragment implements PokemonGestureListener.Listener {

    private static final String TAG = PokemonDetailFragment.class.getSimpleName();

    /* BUNDLE KEYS */
    private static final String KEY_POKEMON_ID = "KEY_POKEMON_ID";
    private static final String KEY_TRANSITION_NAME = "KEY_TRANSITION_NAME";
    private static final String KEY_SHOW_NAVIGATION = "KEY_SHOW_NAVIGATION";

    /* VIEWS */
    // TODO TOUT EST A FAIRE AVEC BUTTERKNIFE ET LES VUES SONT CELLES CREES DANS pokemon_detail_layout
    // TODO 24) BINDER LE CONSTRAINTLAYOUT GLOBAL
    @BindView(R.id.cl_pokemon_detail)
    public ConstraintLayout pokemonDetails;

    // TODO 25) BINDER L'IMAGEVIEW DE L'IMAGE DU POKEMON
    @BindView(R.id.iv_pokemon_image)
    public ImageView pokemonImage;

    // TODO 26) BINDER LA TEXTVIEW POUR LE NUMERO DU POKEMON
    @BindView(R.id.tv_pokemon_number)
    public TextView pokemonNumber;

    // TODO 27) BINDER LA TEXTVIEW POUR LE NOM DU POKEMON
    @BindView(R.id.tv_pokemon_name)
    public TextView pokemonName;

    // TODO 28) BINDER LA TEXTVIEW POUR L'EXP DE BASE DU POKEMON
    @BindView(R.id.tv_pokemon_base_exp)
    public TextView pokemonExp;

    // TODO 29) BINDER LA TEXTVIEW POUR LA TAILLE DU POKEMON
    @BindView(R.id.tv_pokemon_ht)
    public TextView pokemonHeight;

    // TODO 30) BINDER LA TEXTVIEW POUR LE POIDS DU POKEMON
    @BindView(R.id.tv_pokemon_wt)
    public TextView pokemonWeight;

    // TODO 31) BINDER LA TEXTVIEW POUR LE TYPE DU POKEMON
    @BindView(R.id.tv_pokemon_type)
    public TextView pokemonType;

    /* ATTRIBUTES */
    private int pokemonId;
    private View rootView;
    private boolean isNavigationShown = true;
    private int idMaxPokemon;

    /**
     * @param pokemonId id of the pokemon shown
     * @return newInstance of PokemonDetailFragment
     */
    public static PokemonDetailFragment newInstance(int pokemonId) {
        PokemonDetailFragment pokemonDetailFragment = new PokemonDetailFragment();

        // TODO 14) INSTANCIER UN Bundle ET INSERER DEDANS LE pokemonId AVEC LA CLE KEY_POKEMON_ID
        Bundle nvBundle = new Bundle();
        nvBundle.putInt(KEY_POKEMON_ID, pokemonId);

        // TODO 15) SETTER COMME ARGUMENTS LE Bundle A pokemonDetailFragment
        pokemonDetailFragment.setArguments(nvBundle);

        return pokemonDetailFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getArguments() != null) {
            // TODO 16) RECUPERER DANS getArguments LE POKEMON ID AVEC LA CLE KEY_POKEMON_ID ET ENREGISTRER LE DANS this.pokemonId
            this.pokemonId = getArguments().getInt(KEY_POKEMON_ID);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Let's know the fragment that we may have a transition to show
        PokedexApp.app().component().inject(this);
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //TODO 13) RETOURNER LA VUE pokemon_detail_layout EN TANT QUE rootView (UTILISER inflater POUR inflate LA XML)
        rootView = inflater.inflate(R.layout.pokemon_detail_layout, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initActionBar(isNavigationShown, null);

        //If we can show the navigation, we can then swipe between pokemons
        if (isNavigationShown) {
            // TODO 36) INSTANCIER UN PokemonGestureListener ET SETTER LE onTouchListener du CONSTRAINTLAYOUT GLOBAL DE pokemon_detail_layout AVEC
            PokemonGestureListener monPokemonGestureListener = new PokemonGestureListener(this, null, getContext());
            pokemonDetails.setOnTouchListener(monPokemonGestureListener);
        }

        //Initialisation and observation of the ViewModel for this screen
        PokemonViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(PokemonViewModel.class);

        // TODO 17) APPELER init AVEC pokemonId DEPUIS viewModel POUR RECUPERER LES INFORMATIONS DU POKEMON A AFFICHER
        viewModel.init(this.pokemonId);
        //Once we get the pokemon from the ViewModel or if he is updated we call initView
        viewModel.getPokemon().observe(this, pokemon -> {
            // TODO 18) APPELER initView AVEC LE POKEMON RENVOYER PAR LE VIEWMODEL
            initView(pokemon);
        });
        //Once we get the number max of pokemon from the ViewModel or if he is updated we call update the value
        //That allow us to not swipe further than the last one in database
        viewModel.getIdMaxPokemon().observe(this, integer -> idMaxPokemon = integer != null ? integer : 0);
    }

    /**
     * Initialise the view with the given pokemon
     *
     * @param pokemon {@link Pokemon} to show
     */
    private void initView(Pokemon pokemon) {
        if (pokemon != null) {
            if (getContext() != null) {
                // TODO 32) UTILISER GLIDE POUR TELECHARGER L'IMAGE DU POKEMON DANS L'IMAGEVIEW
                RequestOptions request = new RequestOptions();
                request.placeholder(R.drawable.ic_launcher_pokeball);
                Glide.with(getContext()).setDefaultRequestOptions(request).load(pokemon.getSpritesString()).into(pokemonImage);
            }

            //If we can Navigate between pokemons we show his name on the actionBar, else we keep the default name
            // TODO 33) APPELER setTitle POUR AFFICHER LE NOM DU POKEMON
            setTitle(isNavigationShown ? pokemon.getName() : null);

            // TODO 34) POUR CHAQUE VUE ASSOCIER LA BONNE DONNEE (DES STRINGS SONT A DISPOSITION POUR CERTAINS CHAMPS PENSEZ A LES UTILISER)
            pokemonNumber.setText(pokemon.getStringId());
            pokemonName.setText(pokemon.getName());
            pokemonExp.setText(pokemon.getStringBaseExp());
            pokemonHeight.setText(pokemon.getStringHeight());
            pokemonWeight.setText(pokemon.getStringWeight());

            // TODO 35) UN POKEMON PEUT AVOIR PLUSIEURS TYPES, N'AFFICHER QUE LE PREMIER POUR LE MOMENT (BONUS SI VOUS AFFICHEZ TOUT LES TYPES D'UN POKEMON)
            pokemonType.setText(pokemon.getTypes().get(0).getType().getName());
        }
    }

    @Override
    public void onSwipe(int direction) {
        // TODO 37) VERIFIER LA DIRECTION avec PokemonGestureListener.LEFT ou .RIGHT ET APPELER navigationManager POUR AFFICHER LE DETAIL DU POKEMON SUIVANT OU PRECEDENT
        if (direction == PokemonGestureListener.LEFT && pokemonId -1 > 0) {
            navigationManager.startPokemonDetail(pokemonId - 1, false);
        } else if (direction == PokemonGestureListener.RIGHT && pokemonId + 1 <= idMaxPokemon) {
            navigationManager.startPokemonDetail(pokemonId + 1, false);
        }
    }
}