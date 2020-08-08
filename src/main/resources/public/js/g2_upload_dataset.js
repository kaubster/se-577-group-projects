const upload_dataset_component = new Vue({
    el: "#upload_dataset",
    data: {
      datasets: []
    },
    methods: {      
    log(status){
      var error = document.getElementById("error");
      console.log(status);
      if (typeof status.statusText != 'undefined')
        error.innerText = status.statusText;
      else
        error.innerText = status;
    },
    datasetNameChanged() {
      var aDatasetName = document.getElementById("name").value;
      var aUploadButton = document.getElementById("upload"); 

      if(!aUploadButton){
        return;
      }

      if(!aDatasetName){
        return;
      }

      var bailBadDatasetName = false;
      if(this.datasets){
        this.datasets.forEach(function(t) {
          if(t.name === aDatasetName){
            bailBadDatasetName = true;
          }
        }); 
      }

      if(bailBadDatasetName){        
        this.log("Dataset name exists.");
        aUploadButton.disabled = true;
        return;
      }
      
      aUploadButton.disabled = false;
    },
    fetchDatasets(){
      fetch("/api/datasets")
      .then(response => response.json())
      .then((data) => {
        this.datasets = data;
      })
    },
    attemptUpload() {        
        var input = document.querySelector('input[type="file"]')
        var dataSetName = document.getElementById("name").value;
        
        if( dataSetName !== "" ) {
          function handleErrors(response) {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            return response;
          }
          
          // Content-types
          // CSV and XLS : "application/vnd.ms-excel"
          // XLSX : application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

          if(input === null || input.files === null || input.files.length === 0){
            this.log("Select a file.");
            return;
          }

          var file = input.files[0];
          //var type = file.type; // bah lets provide an expected value
          var fname = file.name;
          
          var ext = fname.slice((Math.max(0, fname.lastIndexOf(".")) || Infinity) + 1);
          if(ext === null || ext === undefined){
            this.log("Unable to determine file type. Select a file with an extension.");
            return;
          }
          var type = "application/" + ext;          

          fetch("/api/datasets?name=" + dataSetName, {
            body: file,
            method: "POST",
            headers: {
              "Content-Type": type,
            },
          })
          .then(handleErrors).then(
            (success) => {
              this.log(success);
              this.fetchDatasets();  
            }
          ).catch(
            error => this.log(error) // Handle the error response object
          );
        } else {
          this.log("Select a file.");
        }
      }
    },
    // beforeCreate(){

    // },
    // beforeMount(){

    // },
    // beforeDestroyed(){

    // },
    // destroyed(){

    // },
    mounted() {
      this.fetchDatasets();
    },
    template: ` 
      <div>
        <div class="tabcontent">
        <h3>Upload your dataset</h3>
          <form action="">
          Dataset Name:<br>
          <input type="text" name="name" id="name" v-on:change="datasetNameChanged">
          <br>
          </form>
          <br>
            <div>
              <label for="files" class="btn">Select XLS, XLSX or CSV:</label>
              <input type="file" id="myFile" accept=".xls,.xlsx,.mat,.csv"/>
            </div>
          <br>
          <button id="upload" v-on:click="attemptUpload()" disabled="disable">Upload</button>
          <label for="upload">
            <span class="error" name="error" id="error"></span>
          </label>
          <br>
      </div>
      <br>

      <div class="card mb-3">
        <div class="card-header">
          <i class="fa fa-table"></i>Existing Datasets</div>
        <div class="card-body">
          <div class="table-responsive">        
            <table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">
              <thead>
                <tr>
                  <th>Definition Name</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="dataset, i in datasets">
                  <td>{{dataset.name}}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>          
    `,
});