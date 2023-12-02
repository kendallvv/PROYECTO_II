import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieSearch extends JFrame {
/**
 * Clave de API y URL base de la API de The Movie Database (TMDb)
 *
 */
    private static final String API_KEY = "eba71768ad1290ad4dc8d305cef81e0a";
    private static final String BASE_URL = "https://api.themoviedb.org/3/search/movie";
/**
 *  Lista para almacenar información de películas
 *
 */
    
    private final List<MovieInfo> movieInfoList = new ArrayList<>();
/**
 *  Variables para el control de paginación y búsqueda
 *
 */
    
    private int currentPage = 1;
    private boolean isSearching = false;
    private String lastSearchTerm = "";
/**
 * // Constructor de la clase principal
 *
 */
    public MovieSearch() {
        // Configuración de la ventana principal
        setTitle("Movie Search");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Creación de componentes de la interfaz gráfica
        JPanel mainPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JPanel resultPanel = new JPanel(new GridLayout(0, 1));
        JScrollPane scrollPane = new JScrollPane(resultPanel);

        JPanel topPanel = new JPanel();
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(prevButton);
        topPanel.add(nextButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Manejo de eventos para botones de búsqueda y paginación
        searchButton.addActionListener(e -> {
            currentPage = 1;
            String searchTerm = searchField.getText();
            if (!searchTerm.isEmpty()) {
                isSearching = true;
                lastSearchTerm = searchTerm;
                searchMovies(searchTerm, resultPanel);
            } else {
                JOptionPane.showMessageDialog(MovieSearch.this, "Por favor, ingrese un término de búsqueda",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadMovies(resultPanel);
            } else if (isSearching) {
                isSearching = false;
                loadMovies(resultPanel);
            }
        });

        nextButton.addActionListener(e -> {
            currentPage++;
            loadMovies(resultPanel);
        });
        loadMovies(resultPanel);
    }
/**
 * Para cargar películas desde la API
 *
 */
    
    private void loadMovies(JPanel resultPanel) {
        try {
            String apiUrl;
            if (isSearching) {
                apiUrl = BASE_URL + "?api_key=" + API_KEY + "&query=" +
                        lastSearchTerm + "&page=" + currentPage;
            } else {
                apiUrl = "https://api.themoviedb.org/3/movie/popular?api_key="
                        + API_KEY + "&page=" + currentPage;
            }

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parseo de la respuesta JSON de la API
            JsonObject jsonObject = JsonParser.parseString(response.toString())
                    .getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");

            // Limpieza y actualización del panel de resultados
            resultPanel.removeAll();
            movieInfoList.clear();

            // Iteración sobre los resultados para extraer información
            for (int i = 0; i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();
                JsonElement titleElement = movie.get("title");
                JsonElement posterPathElement = movie.get("poster_path");
                JsonElement idElement = movie.get("id");

                if (titleElement != null && !titleElement.isJsonNull() &&
                        posterPathElement != null &&
                        !posterPathElement.isJsonNull()
                        && idElement != null && !idElement.isJsonNull()) {
                    String title = titleElement.getAsString();
                    String posterPath = posterPathElement.getAsString();
                    String imageUrl = "https://image.tmdb.org/t/p/w500" +
                            posterPath;

                    // Creación de etiquetas de película y actualización de la lista
                    JLabel movieLabel = createMovieLabel(title, imageUrl);
                    resultPanel.add(movieLabel);

                    movieInfoList.add(new MovieInfo(title, imageUrl,
                            idElement.getAsInt()));
                }
            }

            // Actualización del panel y repintado
            resultPanel.revalidate();
            resultPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error cargando películas", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
/**
 * Para buscar películas por un término específico
 *
 */
    private void searchMovies(String searchTerm, JPanel resultPanel) {
        try {
            String apiUrl = BASE_URL + "?api_key=" + API_KEY + "&query=" +
                    searchTerm + "&page=" + currentPage;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parseo de la respuesta JSON de la API
            JsonObject jsonObject = JsonParser.parseString(response.toString())
                    .getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");

            // Limpieza y actualización del panel de resultados
            resultPanel.removeAll();
            movieInfoList.clear();

            // Iteración sobre los resultados para extraer información
            for (int i = 0; results != null && i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();
                JsonElement titleElement = movie.get("title");
                JsonElement posterPathElement = movie.get("poster_path");
                JsonElement idElement = movie.get("id");

                if (titleElement != null && !titleElement.isJsonNull() && posterPathElement
                        != null && !posterPathElement.isJsonNull() && idElement
                        != null && !idElement.isJsonNull()) {
                    String title = titleElement.getAsString();
                    String posterPath = posterPathElement.getAsString();
                    String imageUrl = "https://image.tmdb.org/t/p/w500" +
                            posterPath;

                    // Creación de etiquetas de película y actualización de la lista
                    JLabel movieLabel = createMovieLabel(title, imageUrl);
                    resultPanel.add(movieLabel);

                    movieInfoList.add(new MovieInfo(title, imageUrl,
                            idElement.getAsInt()));
                }
            }

            // Actualización del panel y repintado
            resultPanel.revalidate();
            resultPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error buscando películas",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
/**
 * Para crear una etiqueta de película con imagen y título
 *
 */
    private JLabel createMovieLabel(String title, String imageUrl) {
        JLabel movieLabel = new JLabel(title);

        // Carga asíncrona de la imagen de la película
        new Thread(() -> {
            try {
                URL imageURL = new URL(imageUrl);
                BufferedImage img = ImageIO.read(imageURL);

                int width = 150;
                int height = 225;
                Image scaledImg = img.getScaledInstance(width, height,
                        Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImg);

                SwingUtilities.invokeLater(() -> {
                    movieLabel.setIcon(icon);
                    movieLabel.revalidate();
                    movieLabel.repaint();
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();

        // Manejo de clic en la etiqueta para mostrar información detallada
        movieLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int movieId = movieInfoList.stream()
                        .filter(movieInfo -> movieInfo.getTitle().equals(title))
                        .findFirst()
                        .map(MovieInfo::getMovieId)
                        .orElse(-1);
                showMovieInformation(title, movieId);
            }
        });

        return movieLabel;
    }
/**
 * Para mostrar información detallada de una película
 *
 */
    // Método para mostrar información detallada de una película
    private void showMovieInformation(String title, int movieId) {
        try {
            String apiUrl = "https://api.themoviedb.org/3/movie/" + movieId +
                    "?api_key=" + API_KEY + "&append_to_response=credits,videos";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parseo de la respuesta JSON de la API
            JsonObject movieDetails = JsonParser.parseString(response.toString())
                    .getAsJsonObject();

            // Extracción de información detallada de la película
            String director = extractDirector(movieDetails);
            String synopsis = movieDetails.get("overview").getAsString();
            List<String> genres = extractGenres(movieDetails);
            String trailerLink = extractTrailerLink(movieDetails);
            List<String> actors = extractActors(movieDetails);

            // Construcción del mensaje de información
            StringBuilder infoMessage = new StringBuilder();
            infoMessage.append("Título: ").append(title).append("\n");
            infoMessage.append("Director: ").append(director).append("\n");
            infoMessage.append("Actores: ").append(String.join(", ", actors)).append("\n");
            infoMessage.append("Sinopsis: \n").append(synopsis).append("\n");
            infoMessage.append("Géneros: ").append(String.join(", ", genres)).append("\n");

            // Creación de botones y panel de información detallada
            JButton closeButton = new JButton("Cerrar");
            JButton trailerButton = new JButton("Ver Tráiler");

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            buttonPanel.add(trailerButton);

            JPanel dialogPanel = new JPanel(new BorderLayout());
            JTextArea synopsisTextArea = new JTextArea(infoMessage.toString());
            synopsisTextArea.setLineWrap(true);
            synopsisTextArea.setWrapStyleWord(true);
            synopsisTextArea.setEditable(false);
            synopsisTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            dialogPanel.add(new JScrollPane(synopsisTextArea), BorderLayout.CENTER);
            dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Creación y configuración del diálogo
            JDialog movieDialog = new JDialog(this, "Información de la Película", true);
            movieDialog.getContentPane().add(dialogPanel);
            movieDialog.setSize(400, 300);
            movieDialog.setLocationRelativeTo(this);

            // Manejo de eventos de botones
            closeButton.addActionListener(e -> movieDialog.dispose());

            trailerButton.addActionListener(e -> {
                if (!trailerLink.isEmpty()) {
                    try {
                        // Apertura del enlace del tráiler en el navegador
                        Desktop.getDesktop().browse(new URL(trailerLink).toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // Visualización del diálogo
            movieDialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error obteniendo detalles de la película",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
/**
 * Para extraer el director de los detalles de la película
 *
 */
    
    private String extractDirector(JsonObject movieDetails) {
        JsonObject credits = movieDetails.getAsJsonObject("credits");
        for (JsonElement crewElement : credits.getAsJsonArray("crew")) {
            JsonObject crew = crewElement.getAsJsonObject();
            if ("Director".equals(crew.get("job").getAsString())) {
                return crew.get("name").getAsString();
            }
        }
        return "N/A";
    }
/**
 * Para extraer los géneros de los detalles de la película
 *
 */
    
    private List<String> extractGenres(JsonObject movieDetails) {
        JsonArray genres = movieDetails.getAsJsonArray("genres");
        List<String> genreList = new ArrayList<>();

        for (JsonElement genreElement : genres) {
            JsonObject genreObject = genreElement.getAsJsonObject();
            JsonElement nameElement = genreObject.get("name");

            if (nameElement != null && !nameElement.isJsonNull()) {
                genreList.add(nameElement.getAsString());
            }
        }

        return genreList;
    }
/**
 *  Para extraer el enlace del tráiler de los detalles de la película
 */
    private String extractTrailerLink(JsonObject movieDetails) {
        JsonObject videos = movieDetails.getAsJsonObject("videos");
        for (JsonElement videoElement : videos.getAsJsonArray("results")) {
            JsonObject video = videoElement.getAsJsonObject();
            if ("Trailer".equals(video.get("type").getAsString())) {
                return "https://www.youtube.com/watch?v=" + video.get("key")
                        .getAsString();
            }
        }
        return "";
    }


      /**
     * Extrae la lista de actores de los detalles de una película proporcionados por la API de TMDb.
     *
     */
    private List<String> extractActors(JsonObject movieDetails) {
        JsonObject credits = movieDetails.getAsJsonObject("credits");
        JsonArray cast = credits.getAsJsonArray("cast");
        List<String> actors = new ArrayList<>();

        // Recorre la matriz de actores y extrae los nombres.
        for (JsonElement castElement : cast) {
            JsonObject actor = castElement.getAsJsonObject();
            JsonElement nameElement = actor.get("name");

            if (nameElement != null && !nameElement.isJsonNull()) {
                actors.add(nameElement.getAsString());
            }
        }

        return actors;
    }

    /**
     * Método principal que inicia la aplicación de búsqueda de películas.
     *
     */
    public static void main(String[] args) {
        // Invoca la creación y visualización de la interfaz gráfica en el hilo de eventos de Swing.
        SwingUtilities.invokeLater(() -> {
            MovieSearch frame = new MovieSearch();
            frame.setVisible(true);
        });
    }
}

/**
 * Clase que representa información sobre una película.
 */
class MovieInfo {
    private String title;
    private String imageUrl;
    private int movieId;

    /**
     * Constructor de la clase MovieInfo.
     *
     */
    public MovieInfo(String title, String imageUrl, int movieId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.movieId = movieId;
    }

    /**
     * Getter para obtener el título de la película.
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter para obtener la URL de la imagen de la película.
     *
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Getter para obtener el ID único de la película.
     *
     */
    public int getMovieId() {
        return movieId;
    }
}








