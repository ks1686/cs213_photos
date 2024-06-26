package model;

// Java imports
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Represents an album of photos. An album has a name and a list of photos. An
 * album can be created with a name and a list of photos. An album can have
 * photos added to it, removed from it, and moved to another album. An album can
 * be searched for photos based on tags or dates. An album can have its name
 * changed. An album can have its start and end dates retrieved.
 *
 * @author jacobjude
 */
public class Album implements Serializable {
    private String albumName;
    private List<Photo> photos;

    /**
     * Creates an album with the given name and an empty list of photos.
     *
     * @param albumName the name of the album
     * @throws NullPointerException     if albumName is null
     * @throws IllegalArgumentException if albumName is empty
     */
    public Album(String albumName) throws NullPointerException, IllegalArgumentException {
        this(albumName, new ArrayList<>());
    }

    /**
     * Creates an album with the given name and list of photos.
     *
     * @param albumName the name of the album
     * @param photos    the list of photos in the album
     * @throws NullPointerException     if albumName or photos is null
     * @throws IllegalArgumentException if albumName is empty
     */
    public Album(String albumName, List<Photo> photos) throws NullPointerException, IllegalArgumentException {
        if (albumName == null) {
            throw new NullPointerException("albumName cannot be null");
        } else if (albumName.isEmpty()) {
            throw new IllegalArgumentException("albumName cannot be empty");
        }
        if (photos == null) {
            throw new NullPointerException("photos cannot be null");
        }

        this.albumName = albumName;
        this.photos = photos;
    }

    /**
     * Adds a photo to the album.
     *
     * @param photo the photo to add
     */
    public void addPhoto(Photo photo) {
        this.photos.add(photo); // may need to catch an exception here?
    }

    /**
     * Adds a photo to the album.
     *
     * @param filepath the filepath of the photo to add
     */
    public void addPhoto(String filepath) {
        this.photos.add(new Photo(filepath)); // may need to catch an exception here?
    }

    /**
     * Removes a photo from the album.
     *
     * @param photo the photo to remove
     */
    public void removePhoto(Photo photo) {
        this.photos.remove(photo);
    }

    /**
     * Gets a photo from the album.
     *
     * @return arrayList of photos
     */
    public List<Photo> getPhotos() {
        return new ArrayList<>(this.photos);
    }

    /**
     * get the name of the album
     *
     * @return the name of the album
     */
    public String getAlbumName() {
        return albumName;
    }

    /**
     * get the size of the album
     *
     * @return the size of the album
     */
    public int getSize() {
        return this.photos.size();
    }

    /**
     * set the name of the album
     *
     * @param albumName the name of the album
     * @throws NullPointerException     if albumName is null
     * @throws IllegalArgumentException if albumName is empty
     */
    public void setAlbumName(String albumName) throws NullPointerException, IllegalArgumentException {
        if (albumName == null) {
            throw new NullPointerException("albumName cannot be null");
        } else if (albumName.isEmpty()) {
            throw new IllegalArgumentException("albumName cannot be empty");
        }
        this.albumName = albumName;
    }

    /**
     * toString method for the album
     *
     * @return the string representation of the album
     */
    public String toString() {
        // get the toString of all the photos in the album and album name
        String result = "";
        for (Photo photo : this.photos) {
            result += photo.toString() + "\n";
        }
        return "Album: " + this.albumName + "\nPhotos:\n" + result;
    }

    /**
     * get the start date of the album
     *
     * @return the start date of the album
     */
    public Calendar getStartDate() {
        if (this.photos.isEmpty()) {
            return null;
        }
        Calendar startDate = this.photos.getFirst().getDate();
        for (Photo photo : this.photos) {
            if (photo.getDate().compareTo(startDate) < 0) {
                startDate = photo.getDate();
            }
        }
        return startDate;
    }

    /**
     * get the end date of the album
     *
     * @return the end date of the album
     */
    public Calendar getEndDate() {
        if (this.photos.isEmpty()) {
            return null;
        }
        Calendar endDate = this.photos.getFirst().getDate();
        for (Photo photo : this.photos) {
            if (photo.getDate().compareTo(endDate) > 0) {
                endDate = photo.getDate();
            }
        }
        return endDate;
    }

    /**
     * search for photos in the album based on a query
     *
     * @param query the query to search for
     * @return the list of photos that match the query
     * @throws IllegalArgumentException if the query is invalid
     */
    public List<Photo> search(String query) {
        // query can be a tag or a Calendar date
        // check if the query is a calendar date
        List<Photo> result = new ArrayList<>();
        // date will be in the format MM/DD/YYYY-MM/DD/YYYY
        if (query.matches("\\d{2}/\\d{2}/\\d{4}-\\d{2}/\\d{2}/\\d{4}")) {
            // query is a date
            String[] dates = query.split("-");
            // dates are in the format MM/DD/YYYY. make calendar objects for them
            String[] startDate = dates[0].split("/");
            String[] endDate = dates[1].split("/");
            Calendar start = Calendar.getInstance();
            start.set(Integer.parseInt(startDate[2]), Integer.parseInt(startDate[0]), Integer.parseInt(startDate[1]));
            // set hour and minute and second and millisecond to -
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = Calendar.getInstance();
            end.set(Integer.parseInt(endDate[2]), Integer.parseInt(endDate[0]), Integer.parseInt(endDate[1]));
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
            end.set(Calendar.MILLISECOND, 0);

            // check if start date is after end date
            if (start.compareTo(end) > 0) {
                throw new IllegalArgumentException("Invalid query");
            }

            for (Photo photo : this.photos) {
                if (photo.getDate().compareTo(start) >= 0 && photo.getDate().compareTo(end) <= 0) {
                    result.add(photo);
                }
            }
            return result;
        }

        // tags can look like "tagname=tagvalue" and can have conjunctions or
        // disjuctions
        // ex. person=John AND location=New York
        // ex. person=John OR location=New York
        // no need to handle more than 1 conjunction or disjunction

        // split the query by " AND " or " OR "
        String[] parts = query.split(" AND | OR ");

        // if the query is a single tag
        if (parts.length == 1) {
            // check if its of the form "tagname=tagvalue"

            String[] tag = query.split("=");
            if (!(query.strip().contains("=") || tag.length != 2 || tag[0].isEmpty() || tag[1].isEmpty())) {
                throw new IllegalArgumentException("Invalid query");
            }

            if (tag[0].contains(" ") || tag[1].contains(" ")) {
                throw new IllegalArgumentException("Invalid query");
            }
            for (Photo photo : this.photos) {
                for (Map<String, String> currentTag : photo.getTags()) {
                    if (currentTag.containsKey(tag[0]) && currentTag.containsValue(tag[1])) {
                        result.add(photo);
                    }
                }
            }
            return result;
        } else if (parts.length == 2) {
            // if the query is a disjunction
            if (query.contains(" OR ")) {
                String[] tag1 = parts[0].split("=");
                String[] tag2 = parts[1].split("=");
                if (tag1[0].contains(" ") || tag1[1].contains(" ")) {
                    throw new IllegalArgumentException("Invalid query");
                }
                if (tag2[0].contains(" ") || tag2[1].contains(" ")) {
                    throw new IllegalArgumentException("Invalid query");
                }
                for (Photo photo : this.photos) {
                    boolean found1 = false;
                    boolean found2 = false;
                    for (Map<String, String> tag : photo.getTags()) {
                        if (tag.containsKey(tag1[0]) && tag.containsValue(tag1[1])) {
                            found1 = true;
                        }
                        if (tag.containsKey(tag2[0]) && tag.containsValue(tag2[1])) {
                            found2 = true;
                        }
                    }
                    if (found1 || found2) {
                        result.add(photo);
                    }
                }
                return result;
            } else if (query.contains(" AND ")) {
                // if the query is a conjunction
                String[] tag1 = parts[0].split("=");
                String[] tag2 = parts[1].split("=");
                for (Photo photo : this.photos) {
                    boolean found1 = false;
                    boolean found2 = false;
                    for (Map<String, String> tag : photo.getTags()) {
                        if (tag.containsKey(tag1[0]) && tag.containsValue(tag1[1])) {
                            found1 = true;
                        }
                        if (tag.containsKey(tag2[0]) && tag.containsValue(tag2[1])) {
                            found2 = true;
                        }
                    }
                    if (found1 && found2) {
                        result.add(photo);
                    }
                }
                return result;
            }

        }

        throw new IllegalArgumentException("Invalid query");
    }

}