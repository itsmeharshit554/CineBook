import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

class DataOperation {
    // Print available dates
    public static void show_Dates(ArrayList<String> Dates) {
        for (int i = 1; i <= Dates.size(); i++) {
            String temp = convertDateFormat(Dates.get(i - 1));
            System.out.println(i + ". " + temp);
        }
        System.out.println("Enter Date: ");
    }

    // Convert date format
    public static String convertDateFormat(String date) {
        try {
            LocalDate localDate = LocalDate.parse(date); // Parse the date in default format (ISO_LOCAL_DATE)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy"); // Desired format
            return localDate.format(formatter); // Return the formatted date
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format: " + date);
            return date;
        }
    }

    // Show list of movies with their timings and slots
    public static void showMoviesList(ArrayList<String> Movies, ArrayList<String> Timing, ArrayList<String> Slot, ArrayList<Integer> Seats) {
        for (int i = 0; i < Movies.size(); i++) {
            System.out.println((i + 1) + ". " + Movies.get(i) + " " + Slot.get(i) + " " + Timing.get(i) + " --> " + Seats.get(i) + " Seats");
        }
    }

    // Show movie details
    public static void movieDetails(String MovieName, String Description, Float Rating, String Genre, String Actors) {
        System.out.println("Movie Name: " + MovieName);
        System.out.println("Description: " + Description);
        System.out.println("Rating: " + Rating);
        System.out.println("Genre: " + Genre);
        System.out.println("Actors: " + Actors);
    }

    // Handle payment details
    public static void payment(int ticket_booked) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select your Seat Category:");
        System.out.println("1. Diamond --> 300");
        System.out.println("2. Gold --> 200");
        System.out.println("3. Silver --> 100");
        System.out.println("Enter your seat category:");
        int seat_level = sc.nextInt();
        System.out.println("Mode of Payments");
        System.out.println("1. Credit Card");
        System.out.println("2. Debit Card");
        System.out.println("3. UPI");
        System.out.println("4. Net Banking");
        System.out.println("Enter your mode of payment:");
        int mode_pay = sc.nextInt();
        if (seat_level == 1) {
            seat_level = 300;
        } else if (seat_level == 2) {
            seat_level = 200;
        } else if (seat_level == 3) {
            seat_level = 100;
        }
        int total_sum = seat_level * ticket_booked;
        System.out.println("Total Bill is " + total_sum);
    }
}

public class App {
    public static void main(String args[]) {
        System.out.println("   _______            ____              __      \n" +
                "  / ____(_)___  ___  / __ )____  ____  / /__    \n" +
                " / /   / / __ \\/ _ \\/ __  / __ \\/ __ \\/ //_/    \n" +
                "/ /___/ / / / /  __/ /_/ / /_/ / /_/ / ,<       \n" +
                "\\____/_/_/ /_/\\___/_____/\\____/\\____/_/|_|      \n" +
                "                                                ");

        System.out.println("Welcome to Cinebook");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/movie_booking", "root", "Enter_your_password_it_is_just_a_placeholder");

            // Fetch distinct dates from the database
            Statement stmt = con.createStatement();
            ResultSet extract_date = stmt.executeQuery("select DISTINCT date from total_data");
            ArrayList<String> Dates = new ArrayList<>();
            while (extract_date.next()) {
                Dates.add(extract_date.getString(1));
            }
            DataOperation.show_Dates(Dates);

            // Get the user to select a date    
            Scanner sc = new Scanner(System.in);
            int confirm_date = sc.nextInt();
            String confirmed_date = Dates.get(confirm_date - 1);

            // Fetch movie details based on the selected date
            String query = "SELECT name, timing, slot, seats_empty, screening_id, movie_id " +
                    "FROM total_data WHERE date = ?";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, confirmed_date);
            ResultSet extract_movie = pstmt.executeQuery();

            ArrayList<String> Movies = new ArrayList<>();
            ArrayList<String> Timing = new ArrayList<>();
            ArrayList<String> Slot = new ArrayList<>();
            ArrayList<Integer> Seats = new ArrayList<>();
            ArrayList<Integer> ScreeningId = new ArrayList<>();
            ArrayList<Integer> MovieId = new ArrayList<>();
            while (extract_movie.next()) {
                Movies.add(extract_movie.getString("name"));
                Timing.add(extract_movie.getString("timing"));
                Slot.add(extract_movie.getString("slot"));
                Seats.add(extract_movie.getInt("seats_empty"));
                ScreeningId.add(extract_movie.getInt("screening_id"));
                MovieId.add(extract_movie.getInt("movie_id"));
            }
            DataOperation.showMoviesList(Movies, Timing, Slot, Seats);

            System.out.println("Enter Movie: ");
            int selected_movie_index = sc.nextInt();
            int selected_movie_id = MovieId.get(selected_movie_index - 1);
            int ScreenID = ScreeningId.get(selected_movie_index - 1);

            query = "select * from movie_details where id = ?";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, selected_movie_id);
            ResultSet extract_movie_details = pstmt.executeQuery();

            if (extract_movie_details.next()) {
                String MovieName = extract_movie_details.getString("name");
                String Description = extract_movie_details.getString("description");
                Float Rating = extract_movie_details.getFloat("rating");
                String Genre = extract_movie_details.getString("genre");
                String Actors = extract_movie_details.getString("actors");

                DataOperation.movieDetails(MovieName, Description, Rating, Genre, Actors);
            }

            // Ask number of seats to be booked
            System.out.println("Enter number of seats to book:");
            int booking_seats = sc.nextInt();

            query = "select seats_empty from total_data where screening_id = ?";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, ScreenID);
            ResultSet bookTickets = pstmt.executeQuery();

            if (bookTickets.next()) {
                int empty_seats = bookTickets.getInt("seats_empty");
                if (empty_seats >= booking_seats) {
                    int remaining_seats = empty_seats - booking_seats;
                    query = "UPDATE total_data SET seats_empty = ? WHERE screening_id = ?";
                    pstmt = con.prepareStatement(query);
                    pstmt.setInt(1, remaining_seats);
                    pstmt.setInt(2, ScreenID);
                    pstmt.executeUpdate();
                    DataOperation.payment(booking_seats);
                    System.out.println("Thank you for using Cinebook");
                } else {
                    System.out.println("Not enough seats available.");
                }
            }

            // Close resources
            bookTickets.close();
            extract_movie_details.close();
            extract_movie.close();
            extract_date.close();
            pstmt.close();
            stmt.close();
            con.close();
            sc.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("   _______            ____              __      \n" +
                "  / ____(_)___  ___  / __ )____  ____  / /__    \n" +
                " / /   / / __ \\/ _ \\/ __  / __ \\/ __ \\/ //_/    \n" +
                "/ /___/ / / / /  __/ /_/ / /_/ / /_/ / ,<       \n" +
                "\\____/_/_/ /_/\\___/_____/\\____/\\____/_/|_|      \n" +
                "                                                ");


    }
}
