package ru.ekhart86.audiorecorder.list

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_record.view.*
import ru.ekhart86.audiorecorder.R
import ru.ekhart86.audiorecorder.play.PlayActivity


//Класс должен расширять RecyclerView.Adapter
class RecordAdapter(
    private val context: ListRecordActivity,
    private val recordsList: ArrayList<Record>
) :
    RecyclerView.Adapter<RecordAdapter.ViewHolder>() {


    //Создает новый объект ViewHolder всякий раз, когда RecyclerView нуждается в этом.
    //Это тот момент, когда создаётся layout строки списка,
    //передается объекту ViewHolder, и каждый дочерний view-компонент может быть найден и сохранен.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.list_item_record, parent, false)
        return ViewHolder(itemView)
    }

    //Возвращает общее количество элементов списка. Значения списка передаются с помощью конструктора.
    override fun getItemCount(): Int {
        return recordsList.size
    }

    //принимает объект ViewHolder и устанавливает необходимые данные для соответствующей строки во view-компоненте
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var recordNumber = "Запись № ${recordsList[position].id}"
        holder.recordName.text = recordNumber
        holder.dbDateId.text = recordsList[position].date
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlayActivity::class.java)
            intent.putExtra("id", recordsList[position].id)
            intent.putExtra("date", recordsList[position].date)
            startActivity(context, intent, null)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recordName = view.recordName!!
        val dbDateId = view.dbDateId!!
    }
}