package com.github.waitlight.quicktube.sender;

import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class MessageLogService {

    private static final Logger LOG = Logger.getLogger(MessageLogService.class);

    private Connection connection;
    private PreparedStatement preparedStatement;
    private final List<MessageLog> messageLogs = new ArrayList<>();
    private ExecutorService executorService;

    @PostConstruct
    void init() {
        String url = "jdbc:mysql://localhost:3306/quicktube";
        String user = "root";
        String password = "password";

        try {
            connection = DriverManager.getConnection(url, user, password);
            String sql = "INSERT INTO message_log (task_id, status, create_time) VALUES (?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql);
            executorService = Executors.newFixedThreadPool(1);
        } catch (SQLException e) {
            LOG.error("Failed to connect to MySQL", e);
        }
    }

    @PreDestroy
    void destroy() {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (SQLException e) {
            LOG.error("Failed to close MySQL connection", e);
        }
    }

    public void logMessage(String taskId, String status) {
        synchronized (messageLogs) {
            messageLogs.add(new MessageLog(taskId, status));
        }
    }

    @Scheduled(every = "10s")
    void flushLogs() {
        List<MessageLog> logsToFlush;
        synchronized (messageLogs) {
            if (messageLogs.isEmpty()) {
                return;
            }
            logsToFlush = new ArrayList<>(messageLogs);
            messageLogs.clear();
        }

        executorService.submit(() -> {
            try {
                for (MessageLog log : logsToFlush) {
                    preparedStatement.setString(1, log.taskId);
                    preparedStatement.setString(2, log.status);
                    preparedStatement.setObject(3, LocalDateTime.now());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                LOG.infof("Flushed %d message logs to MySQL", logsToFlush.size());
            } catch (SQLException e) {
                LOG.error("Failed to flush message logs to MySQL", e);
            }
        });
    }

    static class MessageLog {
        private final String taskId;
        private final String status;

        public MessageLog(String taskId, String status) {
            this.taskId = taskId;
            this.status = status;
        }
    }
}
