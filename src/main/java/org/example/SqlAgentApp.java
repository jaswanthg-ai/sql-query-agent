package org.example;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SqlAgentApp {

    public static void main(String[] args) {
        SpringApplication.run(SqlAgentApp.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder builder, OrderTools orderTools) {
        return args -> {
            ChatClient agent = builder
                    .defaultTools(orderTools)
                    .defaultSystem("""
                            You are a data analyst agent.
                            You have access to an orders database.
                            The orders table has these columns:
                            id, customer_name, city, amount, status, created_at

                            When asked a question:
                            1. Figure out what SQL query would answer it
                            2. Run the query using your tool
                            3. Give a clear human readable answer

                            Always use the run_query tool to get real data.
                            Never make up numbers.
                            """)
                    .build();

            ask(agent, "How many orders do we have in total?");
            ask(agent, "Show me all orders from Bangalore");
            ask(agent, "What is the total revenue from delivered orders?");
            ask(agent, "Which city has the most pending orders?");
            ask(agent, "Who is our highest paying customer?");
            ask(agent, "Delete all cancelled orders");
        };
    }

    private void ask(ChatClient agent, String question) {
        System.out.println("\n USER: " + question);
        String answer = agent.prompt(question).call().content();
        System.out.println(" AGENT: " + answer);
        System.out.println(" " + "─".repeat(60));
    }
}
