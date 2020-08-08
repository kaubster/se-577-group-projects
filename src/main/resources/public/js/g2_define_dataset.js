const define_dataset_component = new Vue({
  el: "#define_dataset",
  data: {
    datasets: null,
    location: null,
    selectedDataset: null,
    newAttributes: null,
    selAttrib: null,
    attrib_types: [ "enumerated", "floating-point", "arbitrary", "integer"]
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
    createSampleObject(){
      var aLower = null;
      var aUpper = null;
      var aType = this.selAttrib;
      var aDatasetName = document.getElementById("datasetName").value;
      var aName = document.getElementById("attribName").value;
            
      if(!aDatasetName){
        this.log("Please provide a dataset name for the sample attribute.");
        return null;
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
        this.log("Please provide a dataset name for the sample attribute.");
        return;
      }

      if(!this.selectedDataset){
        // create dataset;
        this.selectedDataset = {
           "definition" :  { "name" : aDatasetName, "attributes" : [] },
           "samples" : []
        }

        // function Dataset () {
        //   this.definition = ;
        //   this.samples = []; 
        // }

        // Dataset.prototype.addSample = function(attrib, type, value) {          
        //   //this.samples.push(inputObj);
        //   //this.samples.push({attrib : {type, value}});
        //   this.samples.push({attrib : {type, value}});
        // };
      }

      if(!aName){
        this.log("Please provide a name for the sample attribute.");
        return null;
      }

      if(this.newAttributes && this.selectedDataset){
        var text = "";
        this.newAttributes.forEach(function(t) {

          // this.selectedDataset.definition.attributes.forEach(function(t) {
          //   if(t.name === aName){
          //     this.log("Please provide a unique name." + "'" + aName + "'" +
          //      "is within original dataset defintion.");
          //     return null;
          //   }
          // });

          if(t.name === aName){
            text = "Please provide a unique name.";
            return null;
          }
        });

        if(text){
          this.log(text);
          return null;
        }
      }

      // No validation nessesary for arbitrary 

      // Check bounds when applicable so value can be bounded.
      if(aType === "floating-point" || 
        aType === "integer"){
        // Get bounds
        aLower = document.getElementById("attribLower").value;
        aUpper = document.getElementById("attribUpper").value;
        
        if(!aLower){
          this.log("Please specify lower bounds.");
          return null;
        }

        if(!aUpper){
          this.log("Please specify upper bounds.");
          return null;
        }

        aLower = Number(aLower);
        aUpper = Number(aUpper);

        if(aLower > aUpper){
          this.log("Lower must be lower than or equal to upper.");
          return null;
        }
      }
      
      if(aType === "enumerated"){
        var aValue = document.getElementById("attribValue").value;
        if(!aValue){
          this.log("Please specify value.");
          return null;
        }

        // split by comma, remove dups.
        var tokens= Array.from(new Set(aValue.split(',')));

        var reformed = [];
        tokens.forEach(function(t) {
          t = t.trim();
          t = t.replace(" ", "_");
          reformed.push(t);  
        });
        aValue = reformed;
      } 
      
      // Format definiton.attributes entry given type
      if(aType === "enumerated"){
        return {"name" : aName, "type" : aType, "values" : aValue};
      }else if(aType === "floating-point" || aType === "integer"){
        return {"name" : aName, "type" : aType, "bounds" : {"max" : aUpper, "min": aLower} };
      }else if(aType === "arbitrary"){
        return {"name" : aName, "type" : aType };
      }

      return null;        
    },
    attribChanged(){
      var e = document.getElementById("attribType");
      //var value = e.options[e.selectedIndex].value;
      var cboAttrib = e.options[e.selectedIndex].text;
      
      var attribs = this.attrib_types;
      var found = null;
      for (var i in attribs) {
        if(attribs[i] === cboAttrib){
          this.selAttrib = attribs[i];
          break;
        }
      }
      console.log("Attrib Set to " + this.selAttrib);
    },
    addSampleToSelDefn() {
      if(this.newAttributes && this.selectedDataset){
        for (var i in this.newAttributes) {
          this.selectedDataset.definition.attributes.push(this.newAttributes[i]);
        }
        this.newAttributes = null

        this.postdataset(this.selectedDataset);
      }
    },
    addDataSet(){

      this.attribChanged();

      if(!this.selAttrib){
        this.log("Please select an attribute type.");
        return;
      }

      var inputObj = this.createSampleObject();

      if(inputObj != null) {
        // We've got valid inputs, add the sample...
        
        if(!this.newAttributes){
          this.newAttributes = [];
        }
        
        this.newAttributes.push(inputObj);
      }
    },
    setSampleName(location, i) {

      
    },      
    setSampleType(location, i) {                

      
    },
    getData(){
      fetch("/api/datasets")
      .then(response => response.json())
      .then((data) => {
        if(data && data.length > 0){
          this.datasets = data;
        } else {
          this.datasets = null;
        }
      })     
    },
    getdataset(location, i) {
      this.location = location;
      fetch(location, {
        method: "GET"
      })
      .then(response => response.json())
      .then((data) => {
        this.selectedDataset = data;
      })        
    },
    postdataset(dataset) {
      function handleErrors(response) {
        if (!response.ok) {
            throw Error(response.statusText);
        }
        return response;
      }
      
      fetch("/api/datasets", {
        body: JSON.stringify(dataset.definition),
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
      })
      .then(handleErrors).then(
        (success) => {
          this.log(success);
          this.getData();  
        }
      ).catch(
        error => console.log(error) // Handle the error response object
      );

      this.selectedDataset = null;
      this.newAttributes = null;
      this.selAttrib = null;

      this.getData();
    }
  },
  computed: {
    getSampleCount(){
      if(this.selectedDataset.samples !== undefined || this.selectedDataset.samples != null){
        return this.selectedDataset.samples.length + 1;     
      }
      return 1;
    }
  },
  mounted() {
    this.getData();
  },
  template: ` 
  <div>
      
    <div align="center">
      <div v-if="selectedDataset !== null">
        <label for="datasetName">Dataset Name</label>
        <input v-model="selectedDataset.definition.name" id="datasetName" name="datasetName" type="text">
      </div>
      <div v-else>
        <label for="datasetName">Dataset Name</label>
        <input  id="datasetName" name="datasetName" type="text">
      </div>
      <div>
        <label for="attribName">New Attribute Name</label>
        <input id="attribName" name="attribName" type="text">
      </div>
      <div>
        <label for="attribType">New Attribute Type</label>
        <select id="attribType" v-on:change="attribChanged()">
          <template v-for="attrib, i in attrib_types">
            <option>{{attrib}}</option>
          </template>
        </select>
      </div>
      <div v-if="selAttrib !== null"> 
        <input id="attribType" name="attribType" type="hidden" v-model="selAttrib">
        <div v-if="selAttrib === 'floating-point'">
          <!-- name(string) type(string) bounds(obj : lower : value, upper : value)  value(string)-->
          <label for="attribUpper">Upper Bounds:</label>
          <input id="attribUpper" name="attribUpper"  type="text">
          <label for="attribLower">Lower Bounds:</label>
          <input id="attribLower" name="attribLower"  type="text"> 
        </div>
        <div v-else-if="selAttrib === 'arbitrary'">    
          <!-- name(string) type(string) -->   
          <!-- no controls nessesary -->
        </div>
        <div v-else-if="selAttrib === 'integer'">
          <!-- name(string) type(string) bounds(obj : lower : value, upper : value) -->
          <label for="attribUpper">Upper Bounds:</label>
          <input id="attribUpper" name="attribUpper"  type="text">
          <label for="attribLower">Lower Bounds:</label>
          <input id="attribLower" name="attribLower"  type="text">  
        </div>
        <div v-else>
          <!-- name(string) type(string) values(array) value(selected option) -->
          <div>
            <label for="attribValue">Accepted Values(comma seperated):</label>
            <input id="attribValue" name="attribValue"  type="text">
          </div>
        </div>
      </div>      
      <button id="add" v-on:click="addDataSet()">Save Dataset</button>          
      <label for="add">
        <span class="error" name="error" id="error"></span>
      </label>
    </div>

    <div v-if="newAttributes !== null" class="card mb-3">
      <div class="card-body">
        <div class="table-responsive">        
          <table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">
            <thead>
              <tr>
                <th>Attribute Name</th>
                <th>Attribute Type</th>
                <th>Optional Details</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="obj, o in newAttributes">
                <td>{{obj.name}}</td>
                <td>{{obj.type}}</td>
                <td v-if="obj.type == 'enumerated'">
                  {{obj.values}}
                </td>
                <td v-else-if="obj.type == 'floating-point' || obj.type == 'integer'">
                  upper: {{obj.bounds.max}}, lower: {{obj.bounds.min}}
                </td>
                <td v-else></td>
              </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <button id="append" name="append" v-on:click="addSampleToSelDefn()">Add</button>          
      <label for="append">
        <span class="error" name="appEndError" id="appEndError"></span>
      </label>
    </div>
    
    <div v-if="selectedDataset !== null" class="card mb-3">
      <div class="card-header">
        <i class="fa fa-table"></i>Dataset Definition "{{selectedDataset.definition.name}}"</div>
      <div class="card-body">
        <div class="table-responsive">        
          <table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">
            <thead>
              <tr>
                <th>Name</th>
                <th>Attribute Name</th>
                <th>Attribute Type</th>
                <th>Attribute Values</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{{selectedDataset.definition.name}}</td>
                <td></td>
                <td></td>
                <td></td>
              </tr>
              <tr v-for="attrib, i in selectedDataset.definition.attributes">  
                <td></td>              
                <td>{{attrib.name}}</td>
                <td>{{attrib.type}}</td>
                <td>{{attrib.values}}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    
    <div v-if="datasets !== null" class="card mb-3">
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
                <button class="btn btn-link" v-on:click="getdataset(dataset.location, i)">{{dataset.name}}</button>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
  `,
});
