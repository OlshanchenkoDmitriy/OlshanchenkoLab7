import java.util.*;
import java.text.*;

/** Containing items and calculating price. */
public class ShoppingCart{

    public static final int MAX_TITLE_LENGTH = 32;
    public static final double MIN_PRICE = 0.01;
    public static final double PERCENT = 100.00;
    public static final int ALIGN_CENTER = 0;
    public static final int HALF = 2;
    public static final int ALIGN_LEFT = -1;
    public static final  int ALIGN_RIGHT=1;
    public static final int START_INDEX = 0;
    public static final int NO_DISCOUNT = 0;
    public static final int MIN_QUANTITY_FOR_DISCOUNT = 1;
    public static final int HALF_PRICE = 50;
    public static final int SALE_DISCOUNT = 70;
    public static final int MAX_DISCOUNT = 80;
    public static final int QUANTITY_DISCOUNT_FACTOR = 10;
    private static final int INITIAL_COLUMN_WIDTH = 0;
    private static final int LINE_LENGTH_ADJUSTMENT = -1;
    private static final int EMPTY = 0;

    public static enum ItemType { NEW, REGULAR, SECOND_FREE, SALE }
    /**
     * Container for added items
     */
    private List<Item> items = new ArrayList<Item>();

    /**
     * Tests all class methods.
     */
    public static void main(String[] args) {
        // TODO: add tests for  addItem,formatTicket.
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Apple", 0.99, 5, ItemType.NEW);
        cart.addItem("Banana", 20.00, 4, ItemType.SECOND_FREE);
        cart.addItem("A long piece of toilet paper", 17.20, 1, ItemType.SALE);
        cart.addItem("Nails", 2.00, 500, ItemType.REGULAR);
        System.out.println(cart.formatTicket());
    }

    /** Adds new item.
     * @param title item title 1 to 32 symbols
     * @param price item ptice in USD, > 0
     * @param quantity item quantity, from 1
     * @param type item type
     *
     * @throws IllegalArgumentException if some value is wrong
     */
    public void addItem(String title, double price, int quantity, ItemType type){
        if (title == null || title.length() == 0 || title.length() > MAX_TITLE_LENGTH)
            throw new IllegalArgumentException("Illegal title");
        if (price < MIN_PRICE)
            throw new IllegalArgumentException("Illegal price");
        if (quantity <= 0)
            throw new IllegalArgumentException("Illegal quantity");
        Item item = new Item();
        item.setTitle(title);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setType(type);
        items.add(item);
    }

    /**
     * Formats shopping price.
     * @return string as lines, separated with \n,
     *    first line: # Item    Price Quan. Discount  Total
     *    second line: ---------------------------------------
     *    next lines: NN Title     $PP.PP  Q    DD%  $TT.TT
     *            1 Some title     $.30    2    -    $.60
     *            2 Some very long $100.00 1    50%  $50.00
     *            ...
     *            31 Item 42       $999.00 1000 -    $999000.00
     *    end line: -------------------------------------------
     *    last line: 31                              $999050.60
     * if no items in cart returns "No items." string.
     */
    public String formatTicket(){
        if (items.size() == 0)
            return "No items.";
        List<String[]> lines = new ArrayList<String[]>();
        String[] header = {"#","Item","Price","Quan.","Discount","Total"};
        int[] align = new int[] { ALIGN_RIGHT, ALIGN_LEFT, ALIGN_RIGHT, ALIGN_RIGHT, ALIGN_RIGHT, ALIGN_RIGHT };

        double total = calculateItemsParameters(lines);
        return getFormattedTicketTable(total, lines, header, align);
    }


    /**
     * Calculates the total price of all items in the cart and formats each item as a line in the receipt.
     * @param lines A list of string arrays, where each array represents a line in the receipt.
     * @return The total price of all items in the cart.
     */
    private double calculateItemsParameters(List<String[]> lines) {
        // formatting each line
        double total = 0.00;
        int  index = 0;
        for (Item item : items) {
            convertItemsToTableLines(item, lines, ++index);
            total += item.getTotalPrice();
        }
        return total;
    }


    /**
     * Converts an item to a line in the receipt and adds it to the list of lines.
     * @param item The item to be converted.
     * @param lines The list of lines in the receipt.
     * @param index The index of the item in the list of items.
     */
    private void convertItemsToTableLines(Item item, List<String[]> lines, int index) {
        item.setDiscount(calculateDiscount(item.getType(), item.getQuantity()));
        item.setTotalPrice(item.getPrice() * item.getQuantity() * (PERCENT - item.getDiscount()) / PERCENT);
        lines.add(new String[]{
                String.valueOf(index),
                item.getTitle(),
                MONEY.format(item.getPrice()),
                String.valueOf(item.getQuantity()),
                (item.getDiscount() == 0) ? "-" : (String.valueOf(item.getDiscount()) + "%"),
                MONEY.format(item.getTotalPrice())
        });
    }



    /**
     * Formats the shopping cart into a table.
     * @param total The total price of all items in the cart.
     * @param lines A list of string arrays, where each array represents a line in the receipt.
     * @param header The header of the table.
     * @param align An array indicating the alignment of each column.
     * @return The formatted table as a string.
     */
    private String getFormattedTicketTable(double total, List<String[]> lines, String[] header, int[] align) {
        String[] footer = { String.valueOf(lines.size()),"","","","",
                MONEY.format(total) };

        // Initialize the width of each column
        int[] width =new int[]{INITIAL_COLUMN_WIDTH, INITIAL_COLUMN_WIDTH, INITIAL_COLUMN_WIDTH, INITIAL_COLUMN_WIDTH, INITIAL_COLUMN_WIDTH, INITIAL_COLUMN_WIDTH};

        // Adjust the width of each column based on the content
        for (String[] line : lines)
            adjustColumnWidth(line, width);
        for (int i = 0; i < header.length; i++)
            adjustColumnWidth(header, width);
        for (int i = 0; i < footer.length; i++)
            adjustColumnWidth(footer, width);

        // Calculate line length
        int lineLength = width.length - LINE_LENGTH_ADJUSTMENT;
        for (int w : width)
            lineLength += w;

        // Initialize a StringBuilder to build the table
        StringBuilder sb = new StringBuilder();

        // Build the table with header, lines, and footer
        appendFormattedLine(sb, header, align, width);
        appendSeparator(sb, lineLength);
        for (String[] line : lines) {
            appendFormattedLine(sb, line, align, width);
        }
        if (lines.size() > EMPTY) {
            appendSeparator(sb, lineLength);
        }

        // Return the formatted table as a string
        appendFormattedLine(sb, footer, align, width);
        return sb.toString();
    }

    /**
     * Appends a formatted line to the StringBuilder.
     * @param sb The StringBuilder to append to.
     * @param line The line to be formatted.
     * @param align The alignment for each column.
     * @param width The width of each column.
     */
    private void appendFormattedLine(StringBuilder sb, String[] line, int[] align, int[] width) {
        for (int i = 0; i < line.length; i++)
            appendFormatted(sb, line[i], align[i], width[i]);
        sb.append("\n");
    }

    /**
     * Adjusts the width of each column based on the length of the content.
     * @param line The line of content.
     * @param width The current width of each column.
     */
    private void adjustColumnWidth(String[] line, int[] width) {
        for (int i = 0; i < line.length; i++)
            width[i] = (int) Math.max(width[i], line[i].length());
    }

    /**
     * Appends a separator line to the StringBuilder.
     * @param sb The StringBuilder to append to.
     * @param lineLength The length of the line.
     */
    private void appendSeparator(StringBuilder sb, int lineLength) {
        for (int i = 0; i < lineLength; i++)
            sb.append("-");
        sb.append("\n");
    }


    // --- private section -----------------------------------------------------
    // MONEY is a NumberFormat object that formats numbers as strings in the form "$#.00"
    // It uses a DecimalFormatSymbols object to specify that the decimal separator should be '.'
    private static final NumberFormat MONEY;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        MONEY = new DecimalFormat("$#.00", symbols);

    }

    /** Appends to sb formatted value.
     *  Trims string if its length > width.
     * @param align -1 for align left, 0 for center and +1 for align right.
     */
    public static void appendFormatted(StringBuilder sb, String value, int align, int width){
        if (value.length() > width)
            value = value.substring(START_INDEX,width);
        int before = (align == ALIGN_CENTER)
                ? (width - value.length()) / HALF
                : (align == ALIGN_LEFT) ? 0 : width - value.length();
        int after = width - value.length() - before;
        while (before-- > START_INDEX)
            sb.append(" ");
        sb.append(value);
        while (after-- > START_INDEX)
            sb.append(" ");
        sb.append(" ");
    }

    /**
     * Calculates item's discount.
     * For NEW item discount is 0%;
     * For SECOND_FREE item discount is 50% if quantity > 1
     * For SALE item discount is 70%
     * For each full 10 not NEW items item gets additional 1% discount,
     * but not more than 80% total
     */
    public static int calculateDiscount(ItemType type, int quantity){
        int discount = NO_DISCOUNT;
        switch (type) {
            case NEW:
                return NO_DISCOUNT;
            case REGULAR:
                discount = NO_DISCOUNT;
                break;
            case SECOND_FREE:
                if (quantity > MIN_QUANTITY_FOR_DISCOUNT)
                    discount = HALF_PRICE;
                break;
            case SALE:
                discount = SALE_DISCOUNT;
                break;
        }
        if (discount < MAX_DISCOUNT) {
            discount += quantity / QUANTITY_DISCOUNT_FACTOR;
            if (discount > MAX_DISCOUNT)
                discount = MAX_DISCOUNT;
        }
        return discount;
    }

    /** item info */
    private static class Item {
        private String title;
        private double price;
        private int quantity;
        private ItemType type;
        private int discount;
        private double total;


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public ItemType getType() {
            return type;
        }

        public void setType(ItemType type) {
            this.type = type;
        }

        public int getDiscount() {
            return discount;
        }

        public void setDiscount(int discount) {
            this.discount = discount;
        }

        public double getTotalPrice() {
            return total;
        }

        public void setTotalPrice(double total) {
            this.total = total;
        }
    }
}