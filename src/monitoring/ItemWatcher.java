package monitoring;

import haven.Coord;
import nurgling.NInventory;
import nurgling.NUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemWatcher implements Runnable {


    public static class ItemInfo
    {
        String name;
        double q = -1;
        Coord c;
        String container;

        public ItemInfo(String name, double q, Coord c, String container) {
            this.name = name;
            this.q = q;
            this.c = c;
            this.container = container;
        }
    }

    public java.sql.Connection connection;
    final String sql = "";
    ArrayList<ItemInfo> iis;

    public ItemWatcher(ArrayList<ItemInfo> iis)
    {
        this.iis = iis;
    }

    @Override
    public void run() {
        try {
            // Подготавливаем SQL-запросы
            final String deleteSql = "DELETE FROM storageitems WHERE container = ? AND item_hash NOT IN (?)";
            final String insertSql = "INSERT INTO storageitems (item_hash, name, quality, coordinates, container) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT (item_hash) DO UPDATE SET " +
                    "name = EXCLUDED.name, quality = EXCLUDED.quality, coordinates = EXCLUDED.coordinates";

            // Создаем список хэшей для текущего массива
            ArrayList<String> itemHashes = new ArrayList<>();
            for (ItemInfo item : iis) {
                itemHashes.add(generateItemHash(item));
            }

            // Удаляем старые записи для данного контейнера
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                deleteStatement.setString(1, iis.get(0).container);  // Контейнер берем из первого элемента
                deleteStatement.setString(2, String.join(",", itemHashes));  // Хэши текущих предметов
                deleteStatement.executeUpdate();
            }

            // Вставляем или обновляем новые записи
            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                for (ItemInfo item : iis) {
                    String itemHash = generateItemHash(item);

                    insertStatement.setString(1, itemHash);
                    insertStatement.setString(2, item.name);
                    insertStatement.setDouble(3, item.q);
                    insertStatement.setString(4, item.c.toString());
                    insertStatement.setString(5, item.container);

                    insertStatement.executeUpdate();
                }
            }

            // Подтверждаем транзакцию
            connection.commit();
        } catch (SQLException e) {
            // Откатываем транзакцию в случае ошибки
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();
        }
    }

    // Метод для генерации хэша предмета
    private String generateItemHash(ItemInfo item) {
        // Пример: хэш на основе имени, координат и контейнера
        String data = item.name + item.toString() + item.container;
        return NUtils.calculateSHA256(data);  // Используем SHA-256 для генерации хэша
    }
}