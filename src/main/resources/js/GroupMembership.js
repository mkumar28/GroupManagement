	function myFunction() {
			  // Declare variables 
			  var input, filter, table, tr, td, i;
			  input = document.getElementById("value");
			  filter = input.value.toUpperCase();
			  table = document.getElementById("myTable");
			  tr = table.getElementsByTagName("tr");
			
			  // Loop through all table rows, and hide those who don't match the search query
			  for (i = 0; i < tr.length; i++) {
			    td = tr[i].getElementsByTagName("td")[0];
			    if (td) {
			      if (td.innerHTML.toUpperCase().indexOf(filter) > -1) {
			        tr[i].style.display = "";
			      } else {
			        tr[i].style.display = "none";
			      }
			    } 
			  }
			}
			
		/*Function to exprt the table to csv**/
			function exportTableToCSV(filename) {
			    var csv = [];
			    var rows = document.querySelectorAll("table tr");
			    
			    for (var i = 0; i < rows.length; i++) {
			        var row = [], cols = rows[i].querySelectorAll("td, th");
			        
			        for (var j = 0; j < cols.length; j++) 
			            row.push(cols[j].innerText);
			        
			        csv.push(row.join(","));        
			    }
			
			    // Download CSV file
			    downloadCSV(csv.join("\n"), filename);
			}
	/**Function To download the file to CSV**/
			function downloadCSV(csv, filename) {
			    var csvFile;
			    var downloadLink;
			
			    // CSV file
			    csvFile = new Blob([csv], {type: "text/csv"});
			
			    // Download link
			    downloadLink = document.createElement("a");
			
			    // File name
			    downloadLink.download = filename;
			
			    // Create a link to the file
			    downloadLink.href = window.URL.createObjectURL(csvFile);
			
			    // Hide download link
			    downloadLink.style.display = "none";
			
			    // Add the link to DOM
			    document.body.appendChild(downloadLink);
			
			    // Click download link
			    downloadLink.click();
			}
			
 
     //Typeahead field. 
		/*var search = document.querySelector('#selectGroup');
		var results = document.querySelector('#searchresults');
		var templateContent = document.querySelector('#resultstemplate').content;
		search.addEventListener('keyup', function handler(event) {
		    while (results.children.length) results.removeChild(results.firstChild);
		    var inputVal = new RegExp(search.value.trim(), 'i');
		    var set = Array.prototype.reduce.call(templateContent.cloneNode(true).children, function searchFilter(frag, item, i) {
		        if (inputVal.test(item.textContent) && frag.children.length < 6) frag.appendChild(item);
		        return frag;
		    }, document.createDocumentFragment());
		    results.appendChild(set);
		});*/