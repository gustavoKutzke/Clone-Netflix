package co.tiagoaguiar.netflixremake

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.util.CategoryTask
import android.content.Intent

class MainActivity : AppCompatActivity(),CategoryTask.Callback {

    private lateinit var progress: ProgressBar
    private val categories = mutableListOf<Category>()
    private lateinit var adapter:CategoryAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress = findViewById(R.id.progress_main)

        adapter = CategoryAdapter(categories){ id ->
            val intent = Intent(this@MainActivity,MovieActivity::class.java)
            intent.putExtra("id",id)
            startActivity(intent)
        }

        val rv : RecyclerView = findViewById(R.id.rv_main)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        CategoryTask(this).execute("https://api.tiagoaguiar.co/netflixapp/home?apiKey=7f94b724-ff0b-4485-bde1-96babd46b3f0")

    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }


    override fun onResult(categories: List<Category>) {
        //aqui será quando o CategoryTask chamrá de volta (callback ou listener)

        this.categories.clear()
        this.categories.addAll(categories)
        adapter.notifyDataSetChanged() //Força o adapter chamar de novo o onBindViewHolder e etc...
                                       // recriando a tela principal.
        progress.visibility = View.GONE
    }

    override fun onFailure(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        progress.visibility = View.VISIBLE
    }

}