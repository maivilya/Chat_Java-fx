module ru.gb.chat_first_part_javafx {
    requires javafx.controls;
    requires javafx.fxml;


    exports ru.gb.chat_first_part_javafx.Client;
    opens ru.gb.chat_first_part_javafx.Client to javafx.fxml;
}