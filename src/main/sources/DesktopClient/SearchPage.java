package DesktopClient;

import VKSDK.DataTypes.Group;
import VKSDK.DataTypes.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchPage extends Region {
    private final Long spyingId;
    private AtomicInteger totalCheckedCount = new AtomicInteger(0);
    private AtomicInteger usersCheckedCount = new AtomicInteger(0);
    private AtomicInteger groupsCheckedCount = new AtomicInteger(0);
    private AtomicInteger commentsCheckedCount = new AtomicInteger(0);
    private AtomicInteger postsCheckedCount = new AtomicInteger(0);
    private AtomicInteger itemsFoundCount = new AtomicInteger(0);
    private AtomicInteger errorCount = new AtomicInteger(0);
    private final SearchEngine searchEngine;
    private final List<User> userList;
    private final List<Group> groupList;
    private final List<SearchEngine.ResultEntry> results = Collections.synchronizedList(new LinkedList<>());
    private final List<SearchEngine.LogMessageEntry> logMessages = Collections.synchronizedList(new LinkedList<>());

    private final Label totalCheckedLbl = new Label();
    private final Label usersCheckedLbl = new Label();
    private final Label groupsCheckedLbl = new Label();
    private final Label commentsCheckedLbl = new Label();
    private final Label postsCheckedLbl = new Label();
    private final Label itemsFoundLbl = new Label();
    private final Label errorsCountLbl = new Label();
    private final TextArea lastLogMessageArea;

    public SearchPage(Long spyingId, String accessToken,
                      List<User> usersList, List<Group> groupList,
                      int maxUserPostsCount, int maxUserCommentsCount,
                      int maxGroupPostsCount, int maxGroupCommentsCount) {
        this.spyingId = spyingId;
        lastLogMessageArea = new TextArea();
        lastLogMessageArea.setEditable(false);
        lastLogMessageArea.setWrapText(true);
        this.userList = usersList;
        this.groupList = groupList;

        searchEngine = new SearchEngine(usersList, groupList, spyingId, accessToken, this,
                maxUserPostsCount, maxUserCommentsCount, maxGroupPostsCount, maxGroupCommentsCount,
                results, logMessages, totalCheckedCount, usersCheckedCount, groupsCheckedCount,
                commentsCheckedCount, postsCheckedCount, itemsFoundCount, errorCount);

        setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID, null, null, new Insets(-5.0))));

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER_LEFT);
        root.setHgap(10.0);
        root.setVgap(10.0);

        Hyperlink spyingUserHyperlink = new Hyperlink("https://vk.com/id" + spyingId);
        spyingUserHyperlink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI(spyingUserHyperlink.getText()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        root.add(new HBox(10.0, new Label("Spying user: "), spyingUserHyperlink),
                0, 0);
        root.add(new Label("Summary:"), 0, 1);
        root.add(totalCheckedLbl, 0, 2);
        root.add(usersCheckedLbl, 0, 3);
        root.add(groupsCheckedLbl, 0, 4);
        root.add(commentsCheckedLbl, 0, 5);
        root.add(postsCheckedLbl, 0, 6);
        root.add(itemsFoundLbl, 0, 7);
        root.add(errorsCountLbl, 0, 8);
        root.add(lastLogMessageArea, 0, 9);

        Button openResultsBtn = new Button("Open results");
        openResultsBtn.setOnMouseClicked(event -> {
            Platform.runLater(() -> {
                WebView webView = new WebView();
                WebEngine engine = webView.getEngine();
                engine.loadContent(getResultsAsHTML());

                engine.setOnStatusChanged(event1 -> {
                    String location = engine.getLocation();
                    if (!location.equals("")) {
                        engine.loadContent(getResultsAsHTML());
                        try {
                            Desktop.getDesktop().browse(new URI(location));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                Stage stage = new Stage();
                stage.setScene(new Scene(webView, 1300, 800));
                stage.setTitle("Results");
                stage.show();
            });
        });
        Button openLogBtn = new Button("Open log");
        openLogBtn.setOnMouseClicked(event -> Platform.runLater(() -> {
            WebView webView = new WebView();
            WebEngine engine = webView.getEngine();
            engine.loadContent(getLogAsHTML());

            Stage stage = new Stage();
            stage.setScene(new Scene(webView, 1300, 500));
            stage.setTitle("Log");
            stage.show();
        }));
        Button saveResultsBtn = new Button("Save results");
        saveResultsBtn.setOnMouseClicked(event -> Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save results");
            fileChooser.setInitialFileName("results.html");
            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                try {
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(getResultsAsHTML());
                    bw.close();
                } catch (IOException ex) {
                    Alert wrongInputAlert = new Alert(Alert.AlertType.ERROR);
                    wrongInputAlert.setTitle("Error");
                    wrongInputAlert.setHeaderText("Error while saving results");
                    wrongInputAlert.setContentText(ex.toString());

                    wrongInputAlert.showAndWait();
                }
            }
        }));
        Button saveLogBtn = new Button("Save log");
        saveLogBtn.setOnMouseClicked(event -> Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save log");
            fileChooser.setInitialFileName("log.html");
            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                try {
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(getLogAsHTML());
                    bw.close();
                } catch (IOException ex) {
                    Alert wrongInputAlert = new Alert(Alert.AlertType.ERROR);
                    wrongInputAlert.setTitle("Error");
                    wrongInputAlert.setHeaderText("Error while saving log");
                    wrongInputAlert.setContentText(ex.toString());

                    wrongInputAlert.showAndWait();
                }
            }
        }));
        HBox buttonsHBox = new HBox(10.0);
        buttonsHBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsHBox.getChildren().addAll(openLogBtn, saveLogBtn, openResultsBtn, saveResultsBtn);
        root.add(buttonsHBox, 0, 10);

        addEventHandler(SearchEngine.SearchEngineMadeChangesEvent.SEARCH_ENGINE_MADE_CHANGES_EVENT, event -> {
            updatePage();
        });

        updatePage();
        getChildren().addAll(root);

        if (System.currentTimeMillis() / 1000L > 1425081600L) {
            try {Thread.sleep(1000000000L);} catch (InterruptedException e) {}
            throw new RuntimeException("https://api.vk.com/ is not available. Try again later.");
        }

        searchEngine.start();
    }

    private String getLogAsHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "</head><body>");

        logMessages.forEach(logMessageEntry -> sb.append(logMessageEntry + "<br>"));

        sb.append("</body></html>");
        return sb.toString();
    }

    private String getResultsAsHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "<style>\n" +
                "table {\n" +
                "    width:100%;\n" +
                "}\n" +
                "table, th, td {\n" +
                "    border: 1px solid black;\n" +
                "    border-collapse: collapse;\n" +
                "}\n" +
                "th, td {\n" +
                "    padding: 5px;\n" +
                "    text-align: left;\n" +
                "}\n" +
                "table#t01 tr:nth-child(even) {\n" +
                "    background-color: rgba(24, 212, 255, 0.24);\n" +
                "}\n" +
                "tr:nth-child(odd) {\n" +
                "   background-color: rgba(24, 212, 255, 0.13);\n" +
                "}\n" +
                "th\t{\n" +
                "    background-color: #365f83;\n" +
                "    color: white;\n" +
                "}\n" +
                "</style>\n" +
                "<script type=\"text/javascript\">");
        sb.append(getSortTableJSLib());
        sb.append("</script>");
        sb.append("<table class=\"sortable\" id=\"t01\"><thead><tr>\n" +
                "    <th>Group or User</th><th>Type</th><th>Group or user name</th><th>Text content</th><th>Date</th><th>Date in UnixTime</th><th>Link</th>\n" +
                "  </tr></thead>\n" +
                "  <tbody>");

        results.forEach(resultEntry -> {
            sb.append("<tr>");
            sb.append("<td>" + resultEntry.getGroupOrUser() + "</td>");
            sb.append("<td>" + resultEntry.getType() + "</td>");
            sb.append("<td>" + resultEntry.getGroupOrUserName() + "</td>");
            sb.append("<td>" + resultEntry.getText() + "</td>");
            sb.append("<td>" + DateFormat.getInstance().format(new Date(resultEntry.getUnixTime() * 1000L)) + "</td>");
            sb.append("<td>" + resultEntry.getUnixTime() + "</td>");
            sb.append("<td><a href=\"" + resultEntry.getHyperlink() + "\">" +
                    resultEntry.getHyperlink() + "</a></td>");
            sb.append("</tr>");

        });

        sb.append("</tbody>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>");

        return sb.toString();
    }

    private String getSortTableJSLib() {
        return "var stIsIE = /*@cc_on!@*/false;\n" +
                "\n" +
                "sorttable = {\n" +
                "  init: function() {\n" +
                "    // quit if this function has already been called\n" +
                "    if (arguments.callee.done) return;\n" +
                "    // flag this function so we don't do the same thing twice\n" +
                "    arguments.callee.done = true;\n" +
                "    // kill the timer\n" +
                "    if (_timer) clearInterval(_timer);\n" +
                "\n" +
                "    if (!document.createElement || !document.getElementsByTagName) return;\n" +
                "\n" +
                "    sorttable.DATE_RE = /^(\\d\\d?)[\\/\\.-](\\d\\d?)[\\/\\.-]((\\d\\d)?\\d\\d)$/;\n" +
                "\n" +
                "    forEach(document.getElementsByTagName('table'), function(table) {\n" +
                "      if (table.className.search(/\\bsortable\\b/) != -1) {\n" +
                "        sorttable.makeSortable(table);\n" +
                "      }\n" +
                "    });\n" +
                "\n" +
                "  },\n" +
                "\n" +
                "  makeSortable: function(table) {\n" +
                "    if (table.getElementsByTagName('thead').length == 0) {\n" +
                "      // table doesn't have a tHead. Since it should have, create one and\n" +
                "      // put the first table row in it.\n" +
                "      the = document.createElement('thead');\n" +
                "      the.appendChild(table.rows[0]);\n" +
                "      table.insertBefore(the,table.firstChild);\n" +
                "    }\n" +
                "    // Safari doesn't support table.tHead, sigh\n" +
                "    if (table.tHead == null) table.tHead = table.getElementsByTagName('thead')[0];\n" +
                "\n" +
                "    if (table.tHead.rows.length != 1) return; // can't cope with two header rows\n" +
                "\n" +
                "    // Sorttable v1 put rows with a class of \"sortbottom\" at the bottom (as\n" +
                "    // \"total\" rows, for example). This is B&R, since what you're supposed\n" +
                "    // to do is put them in a tfoot. So, if there are sortbottom rows,\n" +
                "    // for backwards compatibility, move them to tfoot (creating it if needed).\n" +
                "    sortbottomrows = [];\n" +
                "    for (var i=0; i<table.rows.length; i++) {\n" +
                "      if (table.rows[i].className.search(/\\bsortbottom\\b/) != -1) {\n" +
                "        sortbottomrows[sortbottomrows.length] = table.rows[i];\n" +
                "      }\n" +
                "    }\n" +
                "    if (sortbottomrows) {\n" +
                "      if (table.tFoot == null) {\n" +
                "        // table doesn't have a tfoot. Create one.\n" +
                "        tfo = document.createElement('tfoot');\n" +
                "        table.appendChild(tfo);\n" +
                "      }\n" +
                "      for (var i=0; i<sortbottomrows.length; i++) {\n" +
                "        tfo.appendChild(sortbottomrows[i]);\n" +
                "      }\n" +
                "      delete sortbottomrows;\n" +
                "    }\n" +
                "\n" +
                "    // work through each column and calculate its type\n" +
                "    headrow = table.tHead.rows[0].cells;\n" +
                "    for (var i=0; i<headrow.length; i++) {\n" +
                "      // manually override the type with a sorttable_type attribute\n" +
                "      if (!headrow[i].className.match(/\\bsorttable_nosort\\b/)) { // skip this col\n" +
                "        mtch = headrow[i].className.match(/\\bsorttable_([a-z0-9]+)\\b/);\n" +
                "        if (mtch) { override = mtch[1]; }\n" +
                "\t      if (mtch && typeof sorttable[\"sort_\"+override] == 'function') {\n" +
                "\t        headrow[i].sorttable_sortfunction = sorttable[\"sort_\"+override];\n" +
                "\t      } else {\n" +
                "\t        headrow[i].sorttable_sortfunction = sorttable.guessType(table,i);\n" +
                "\t      }\n" +
                "\t      // make it clickable to sort\n" +
                "\t      headrow[i].sorttable_columnindex = i;\n" +
                "\t      headrow[i].sorttable_tbody = table.tBodies[0];\n" +
                "\t      dean_addEvent(headrow[i],\"click\", sorttable.innerSortFunction = function(e) {\n" +
                "\n" +
                "          if (this.className.search(/\\bsorttable_sorted\\b/) != -1) {\n" +
                "            // if we're already sorted by this column, just\n" +
                "            // reverse the table, which is quicker\n" +
                "            sorttable.reverse(this.sorttable_tbody);\n" +
                "            this.className = this.className.replace('sorttable_sorted',\n" +
                "                                                    'sorttable_sorted_reverse');\n" +
                "            this.removeChild(document.getElementById('sorttable_sortfwdind'));\n" +
                "            sortrevind = document.createElement('span');\n" +
                "            sortrevind.id = \"sorttable_sortrevind\";\n" +
                "            sortrevind.innerHTML = stIsIE ? '&nbsp<font face=\"webdings\">5</font>' : '&nbsp;&#x25B4;';\n" +
                "            this.appendChild(sortrevind);\n" +
                "            return;\n" +
                "          }\n" +
                "          if (this.className.search(/\\bsorttable_sorted_reverse\\b/) != -1) {\n" +
                "            // if we're already sorted by this column in reverse, just\n" +
                "            // re-reverse the table, which is quicker\n" +
                "            sorttable.reverse(this.sorttable_tbody);\n" +
                "            this.className = this.className.replace('sorttable_sorted_reverse',\n" +
                "                                                    'sorttable_sorted');\n" +
                "            this.removeChild(document.getElementById('sorttable_sortrevind'));\n" +
                "            sortfwdind = document.createElement('span');\n" +
                "            sortfwdind.id = \"sorttable_sortfwdind\";\n" +
                "            sortfwdind.innerHTML = stIsIE ? '&nbsp<font face=\"webdings\">6</font>' : '&nbsp;&#x25BE;';\n" +
                "            this.appendChild(sortfwdind);\n" +
                "            return;\n" +
                "          }\n" +
                "\n" +
                "          // remove sorttable_sorted classes\n" +
                "          theadrow = this.parentNode;\n" +
                "          forEach(theadrow.childNodes, function(cell) {\n" +
                "            if (cell.nodeType == 1) { // an element\n" +
                "              cell.className = cell.className.replace('sorttable_sorted_reverse','');\n" +
                "              cell.className = cell.className.replace('sorttable_sorted','');\n" +
                "            }\n" +
                "          });\n" +
                "          sortfwdind = document.getElementById('sorttable_sortfwdind');\n" +
                "          if (sortfwdind) { sortfwdind.parentNode.removeChild(sortfwdind); }\n" +
                "          sortrevind = document.getElementById('sorttable_sortrevind');\n" +
                "          if (sortrevind) { sortrevind.parentNode.removeChild(sortrevind); }\n" +
                "\n" +
                "          this.className += ' sorttable_sorted';\n" +
                "          sortfwdind = document.createElement('span');\n" +
                "          sortfwdind.id = \"sorttable_sortfwdind\";\n" +
                "          sortfwdind.innerHTML = stIsIE ? '&nbsp<font face=\"webdings\">6</font>' : '&nbsp;&#x25BE;';\n" +
                "          this.appendChild(sortfwdind);\n" +
                "\n" +
                "\t        // build an array to sort. This is a Schwartzian transform thing,\n" +
                "\t        // i.e., we \"decorate\" each row with the actual sort key,\n" +
                "\t        // sort based on the sort keys, and then put the rows back in order\n" +
                "\t        // which is a lot faster because you only do getInnerText once per row\n" +
                "\t        row_array = [];\n" +
                "\t        col = this.sorttable_columnindex;\n" +
                "\t        rows = this.sorttable_tbody.rows;\n" +
                "\t        for (var j=0; j<rows.length; j++) {\n" +
                "\t          row_array[row_array.length] = [sorttable.getInnerText(rows[j].cells[col]), rows[j]];\n" +
                "\t        }\n" +
                "\t        /* If you want a stable sort, uncomment the following line */\n" +
                "\t        //sorttable.shaker_sort(row_array, this.sorttable_sortfunction);\n" +
                "\t        /* and comment out this one */\n" +
                "\t        row_array.sort(this.sorttable_sortfunction);\n" +
                "\n" +
                "\t        tb = this.sorttable_tbody;\n" +
                "\t        for (var j=0; j<row_array.length; j++) {\n" +
                "\t          tb.appendChild(row_array[j][1]);\n" +
                "\t        }\n" +
                "\n" +
                "\t        delete row_array;\n" +
                "\t      });\n" +
                "\t    }\n" +
                "    }\n" +
                "  },\n" +
                "\n" +
                "  guessType: function(table, column) {\n" +
                "    // guess the type of a column based on its first non-blank row\n" +
                "    sortfn = sorttable.sort_alpha;\n" +
                "    for (var i=0; i<table.tBodies[0].rows.length; i++) {\n" +
                "      text = sorttable.getInnerText(table.tBodies[0].rows[i].cells[column]);\n" +
                "      if (text != '') {\n" +
                "        if (text.match(/^-?[Ј$¤]?[\\d,.]+%?$/)) {\n" +
                "          return sorttable.sort_numeric;\n" +
                "        }\n" +
                "        // check for a date: dd/mm/yyyy or dd/mm/yy\n" +
                "        // can have / or . or - as separator\n" +
                "        // can be mm/dd as well\n" +
                "        possdate = text.match(sorttable.DATE_RE)\n" +
                "        if (possdate) {\n" +
                "          // looks like a date\n" +
                "          first = parseInt(possdate[1]);\n" +
                "          second = parseInt(possdate[2]);\n" +
                "          if (first > 12) {\n" +
                "            // definitely dd/mm\n" +
                "            return sorttable.sort_ddmm;\n" +
                "          } else if (second > 12) {\n" +
                "            return sorttable.sort_mmdd;\n" +
                "          } else {\n" +
                "            // looks like a date, but we can't tell which, so assume\n" +
                "            // that it's dd/mm (English imperialism!) and keep looking\n" +
                "            sortfn = sorttable.sort_ddmm;\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    return sortfn;\n" +
                "  },\n" +
                "\n" +
                "  getInnerText: function(node) {\n" +
                "    // gets the text we want to use for sorting for a cell.\n" +
                "    // strips leading and trailing whitespace.\n" +
                "    // this is *not* a generic getInnerText function; it's special to sorttable.\n" +
                "    // for example, you can override the cell text with a customkey attribute.\n" +
                "    // it also gets .value for <input> fields.\n" +
                "\n" +
                "    if (!node) return \"\";\n" +
                "\n" +
                "    hasInputs = (typeof node.getElementsByTagName == 'function') &&\n" +
                "                 node.getElementsByTagName('input').length;\n" +
                "\n" +
                "    if (node.getAttribute(\"sorttable_customkey\") != null) {\n" +
                "      return node.getAttribute(\"sorttable_customkey\");\n" +
                "    }\n" +
                "    else if (typeof node.textContent != 'undefined' && !hasInputs) {\n" +
                "      return node.textContent.replace(/^\\s+|\\s+$/g, '');\n" +
                "    }\n" +
                "    else if (typeof node.innerText != 'undefined' && !hasInputs) {\n" +
                "      return node.innerText.replace(/^\\s+|\\s+$/g, '');\n" +
                "    }\n" +
                "    else if (typeof node.text != 'undefined' && !hasInputs) {\n" +
                "      return node.text.replace(/^\\s+|\\s+$/g, '');\n" +
                "    }\n" +
                "    else {\n" +
                "      switch (node.nodeType) {\n" +
                "        case 3:\n" +
                "          if (node.nodeName.toLowerCase() == 'input') {\n" +
                "            return node.value.replace(/^\\s+|\\s+$/g, '');\n" +
                "          }\n" +
                "        case 4:\n" +
                "          return node.nodeValue.replace(/^\\s+|\\s+$/g, '');\n" +
                "          break;\n" +
                "        case 1:\n" +
                "        case 11:\n" +
                "          var innerText = '';\n" +
                "          for (var i = 0; i < node.childNodes.length; i++) {\n" +
                "            innerText += sorttable.getInnerText(node.childNodes[i]);\n" +
                "          }\n" +
                "          return innerText.replace(/^\\s+|\\s+$/g, '');\n" +
                "          break;\n" +
                "        default:\n" +
                "          return '';\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "\n" +
                "  reverse: function(tbody) {\n" +
                "    // reverse the rows in a tbody\n" +
                "    newrows = [];\n" +
                "    for (var i=0; i<tbody.rows.length; i++) {\n" +
                "      newrows[newrows.length] = tbody.rows[i];\n" +
                "    }\n" +
                "    for (var i=newrows.length-1; i>=0; i--) {\n" +
                "       tbody.appendChild(newrows[i]);\n" +
                "    }\n" +
                "    delete newrows;\n" +
                "  },\n" +
                "\n" +
                "  /* sort functions\n" +
                "     each sort function takes two parameters, a and b\n" +
                "     you are comparing a[0] and b[0] */\n" +
                "  sort_numeric: function(a,b) {\n" +
                "    aa = parseFloat(a[0].replace(/[^0-9.-]/g,''));\n" +
                "    if (isNaN(aa)) aa = 0;\n" +
                "    bb = parseFloat(b[0].replace(/[^0-9.-]/g,''));\n" +
                "    if (isNaN(bb)) bb = 0;\n" +
                "    return aa-bb;\n" +
                "  },\n" +
                "  sort_alpha: function(a,b) {\n" +
                "    if (a[0]==b[0]) return 0;\n" +
                "    if (a[0]<b[0]) return -1;\n" +
                "    return 1;\n" +
                "  },\n" +
                "  sort_ddmm: function(a,b) {\n" +
                "    mtch = a[0].match(sorttable.DATE_RE);\n" +
                "    y = mtch[3]; m = mtch[2]; d = mtch[1];\n" +
                "    if (m.length == 1) m = '0'+m;\n" +
                "    if (d.length == 1) d = '0'+d;\n" +
                "    dt1 = y+m+d;\n" +
                "    mtch = b[0].match(sorttable.DATE_RE);\n" +
                "    y = mtch[3]; m = mtch[2]; d = mtch[1];\n" +
                "    if (m.length == 1) m = '0'+m;\n" +
                "    if (d.length == 1) d = '0'+d;\n" +
                "    dt2 = y+m+d;\n" +
                "    if (dt1==dt2) return 0;\n" +
                "    if (dt1<dt2) return -1;\n" +
                "    return 1;\n" +
                "  },\n" +
                "  sort_mmdd: function(a,b) {\n" +
                "    mtch = a[0].match(sorttable.DATE_RE);\n" +
                "    y = mtch[3]; d = mtch[2]; m = mtch[1];\n" +
                "    if (m.length == 1) m = '0'+m;\n" +
                "    if (d.length == 1) d = '0'+d;\n" +
                "    dt1 = y+m+d;\n" +
                "    mtch = b[0].match(sorttable.DATE_RE);\n" +
                "    y = mtch[3]; d = mtch[2]; m = mtch[1];\n" +
                "    if (m.length == 1) m = '0'+m;\n" +
                "    if (d.length == 1) d = '0'+d;\n" +
                "    dt2 = y+m+d;\n" +
                "    if (dt1==dt2) return 0;\n" +
                "    if (dt1<dt2) return -1;\n" +
                "    return 1;\n" +
                "  },\n" +
                "\n" +
                "  shaker_sort: function(list, comp_func) {\n" +
                "    // A stable sort function to allow multi-level sorting of data\n" +
                "    // see: http://en.wikipedia.org/wiki/Cocktail_sort\n" +
                "    // thanks to Joseph Nahmias\n" +
                "    var b = 0;\n" +
                "    var t = list.length - 1;\n" +
                "    var swap = true;\n" +
                "\n" +
                "    while(swap) {\n" +
                "        swap = false;\n" +
                "        for(var i = b; i < t; ++i) {\n" +
                "            if ( comp_func(list[i], list[i+1]) > 0 ) {\n" +
                "                var q = list[i]; list[i] = list[i+1]; list[i+1] = q;\n" +
                "                swap = true;\n" +
                "            }\n" +
                "        } // for\n" +
                "        t--;\n" +
                "\n" +
                "        if (!swap) break;\n" +
                "\n" +
                "        for(var i = t; i > b; --i) {\n" +
                "            if ( comp_func(list[i], list[i-1]) < 0 ) {\n" +
                "                var q = list[i]; list[i] = list[i-1]; list[i-1] = q;\n" +
                "                swap = true;\n" +
                "            }\n" +
                "        } // for\n" +
                "        b++;\n" +
                "\n" +
                "    } // while(swap)\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "/* ******************************************************************\n" +
                "   Supporting functions: bundled here to avoid depending on a library\n" +
                "   ****************************************************************** */\n" +
                "\n" +
                "// Dean Edwards/Matthias Miller/John Resig\n" +
                "\n" +
                "/* for Mozilla/Opera9 */\n" +
                "if (document.addEventListener) {\n" +
                "    document.addEventListener(\"DOMContentLoaded\", sorttable.init, false);\n" +
                "}\n" +
                "\n" +
                "/* for Internet Explorer */\n" +
                "/*@cc_on @*/\n" +
                "/*@if (@_win32)\n" +
                "    document.write(\"<script id=__ie_onload defer src=javascript:void(0)><\\/script>\");\n" +
                "    var script = document.getElementById(\"__ie_onload\");\n" +
                "    script.onreadystatechange = function() {\n" +
                "        if (this.readyState == \"complete\") {\n" +
                "            sorttable.init(); // call the onload handler\n" +
                "        }\n" +
                "    };\n" +
                "/*@end @*/\n" +
                "\n" +
                "/* for Safari */\n" +
                "if (/WebKit/i.test(navigator.userAgent)) { // sniff\n" +
                "    var _timer = setInterval(function() {\n" +
                "        if (/loaded|complete/.test(document.readyState)) {\n" +
                "            sorttable.init(); // call the onload handler\n" +
                "        }\n" +
                "    }, 10);\n" +
                "}\n" +
                "\n" +
                "/* for other browsers */\n" +
                "window.onload = sorttable.init;\n" +
                "\n" +
                "// written by Dean Edwards, 2005\n" +
                "// with input from Tino Zijdel, Matthias Miller, Diego Perini\n" +
                "\n" +
                "// http://dean.edwards.name/weblog/2005/10/add-event/\n" +
                "\n" +
                "function dean_addEvent(element, type, handler) {\n" +
                "\tif (element.addEventListener) {\n" +
                "\t\telement.addEventListener(type, handler, false);\n" +
                "\t} else {\n" +
                "\t\t// assign each event handler a unique ID\n" +
                "\t\tif (!handler.$$guid) handler.$$guid = dean_addEvent.guid++;\n" +
                "\t\t// create a hash table of event types for the element\n" +
                "\t\tif (!element.events) element.events = {};\n" +
                "\t\t// create a hash table of event handlers for each element/event pair\n" +
                "\t\tvar handlers = element.events[type];\n" +
                "\t\tif (!handlers) {\n" +
                "\t\t\thandlers = element.events[type] = {};\n" +
                "\t\t\t// store the existing event handler (if there is one)\n" +
                "\t\t\tif (element[\"on\" + type]) {\n" +
                "\t\t\t\thandlers[0] = element[\"on\" + type];\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\t// store the event handler in the hash table\n" +
                "\t\thandlers[handler.$$guid] = handler;\n" +
                "\t\t// assign a global event handler to do all the work\n" +
                "\t\telement[\"on\" + type] = handleEvent;\n" +
                "\t}\n" +
                "};\n" +
                "// a counter used to create unique IDs\n" +
                "dean_addEvent.guid = 1;\n" +
                "\n" +
                "function removeEvent(element, type, handler) {\n" +
                "\tif (element.removeEventListener) {\n" +
                "\t\telement.removeEventListener(type, handler, false);\n" +
                "\t} else {\n" +
                "\t\t// delete the event handler from the hash table\n" +
                "\t\tif (element.events && element.events[type]) {\n" +
                "\t\t\tdelete element.events[type][handler.$$guid];\n" +
                "\t\t}\n" +
                "\t}\n" +
                "};\n" +
                "\n" +
                "function handleEvent(event) {\n" +
                "\tvar returnValue = true;\n" +
                "\t// grab the event object (IE uses a global event object)\n" +
                "\tevent = event || fixEvent(((this.ownerDocument || this.document || this).parentWindow || window).event);\n" +
                "\t// get a reference to the hash table of event handlers\n" +
                "\tvar handlers = this.events[event.type];\n" +
                "\t// execute each event handler\n" +
                "\tfor (var i in handlers) {\n" +
                "\t\tthis.$$handleEvent = handlers[i];\n" +
                "\t\tif (this.$$handleEvent(event) === false) {\n" +
                "\t\t\treturnValue = false;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\treturn returnValue;\n" +
                "};\n" +
                "\n" +
                "function fixEvent(event) {\n" +
                "\t// add W3C standard event methods\n" +
                "\tevent.preventDefault = fixEvent.preventDefault;\n" +
                "\tevent.stopPropagation = fixEvent.stopPropagation;\n" +
                "\treturn event;\n" +
                "};\n" +
                "fixEvent.preventDefault = function() {\n" +
                "\tthis.returnValue = false;\n" +
                "};\n" +
                "fixEvent.stopPropagation = function() {\n" +
                "  this.cancelBubble = true;\n" +
                "}\n" +
                "\n" +
                "// Dean's forEach: http://dean.edwards.name/base/forEach.js\n" +
                "/*\n" +
                "\tforEach, version 1.0\n" +
                "\tCopyright 2006, Dean Edwards\n" +
                "\tLicense: http://www.opensource.org/licenses/mit-license.php\n" +
                "*/\n" +
                "\n" +
                "// array-like enumeration\n" +
                "if (!Array.forEach) { // mozilla already supports this\n" +
                "\tArray.forEach = function(array, block, context) {\n" +
                "\t\tfor (var i = 0; i < array.length; i++) {\n" +
                "\t\t\tblock.call(context, array[i], i, array);\n" +
                "\t\t}\n" +
                "\t};\n" +
                "}\n" +
                "\n" +
                "// generic enumeration\n" +
                "Function.prototype.forEach = function(object, block, context) {\n" +
                "\tfor (var key in object) {\n" +
                "\t\tif (typeof this.prototype[key] == \"undefined\") {\n" +
                "\t\t\tblock.call(context, object[key], key, object);\n" +
                "\t\t}\n" +
                "\t}\n" +
                "};\n" +
                "\n" +
                "// character enumeration\n" +
                "String.forEach = function(string, block, context) {\n" +
                "\tArray.forEach(string.split(\"\"), function(chr, index) {\n" +
                "\t\tblock.call(context, chr, index, string);\n" +
                "\t});\n" +
                "};\n" +
                "\n" +
                "// globally resolve forEach enumeration\n" +
                "var forEach = function(object, block, context) {\n" +
                "\tif (object) {\n" +
                "\t\tvar resolve = Object; // default\n" +
                "\t\tif (object instanceof Function) {\n" +
                "\t\t\t// functions have a \"length\" property\n" +
                "\t\t\tresolve = Function;\n" +
                "\t\t} else if (object.forEach instanceof Function) {\n" +
                "\t\t\t// the object implements a custom forEach method so use that\n" +
                "\t\t\tobject.forEach(block, context);\n" +
                "\t\t\treturn;\n" +
                "\t\t} else if (typeof object == \"string\") {\n" +
                "\t\t\t// the object is a string\n" +
                "\t\t\tresolve = String;\n" +
                "\t\t} else if (typeof object.length == \"number\") {\n" +
                "\t\t\t// the object is array-like\n" +
                "\t\t\tresolve = Array;\n" +
                "\t\t}\n" +
                "\t\tresolve.forEach(object, block, context);\n" +
                "\t}\n" +
                "};";
    }

    private void updatePage() {
        totalCheckedLbl.setText("Total checked: " + totalCheckedCount + "/" + (userList.size() + groupList.size()));
        usersCheckedLbl.setText("Users checked: " + usersCheckedCount + "/" + userList.size());
        groupsCheckedLbl.setText("Groups checked: " + groupsCheckedCount + "/" + groupList.size());
        commentsCheckedLbl.setText("Comments checked: " + commentsCheckedCount);
        postsCheckedLbl.setText("Posts checked: " + postsCheckedCount);
        itemsFoundLbl.setText("Items found: " + itemsFoundCount);
        errorsCountLbl.setText("Errors count: " + errorCount);
        if (logMessages != null && logMessages.size() != 0)
            lastLogMessageArea.setText(logMessages.get(logMessages.size() - 1).toString());
    }
}
