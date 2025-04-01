import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize



//Aqui se crea la entidad y se le asigna un nombre de tabla
@Parcelize
@Entity(tableName = "users")

data class Users (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val correo: String = " ",
    val pass: String = " "

):Serializable, Parcelable