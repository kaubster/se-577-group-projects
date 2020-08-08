Vue.component("series-definition-panel", {
  props:['name', 'datasetLoc', 'attributes'],
  data: function () {
    return {lineAttr: null};
  },
  methods: {
    postSeries: function () {
      var post = {
        name: this.name,
        dataset: this.datasetLoc,
        style: "series",
        attributes: [this.lineAttr],
      };
      fetch("/api/visualizations", {
        body: JSON.stringify(post),
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
      }).then(() => {
        this.$emit("posted-series");
      }, () => {});
    },
  },
  template: `
    <div>
      <label>Attribute:</label>
      <select v-model="lineAttr">
        <option v-for="attr in attributes" v-bind:value="attr">
          {{ attr.name }}
        </option>
      </select>
      <br>
      <button v-on:click="postSeries()">Submit</button>
    </div>
  `
});
Vue.component("histogram-definition-panel", {
    props:['name', 'datasetLoc', 'attributes'],
    data: function () {
        return {binAttr: null};
    },
    methods: {
        postHistogram: function () {
            var post = {
                name: this.name,
                dataset: this.datasetLoc,
                style: "histogram",
                attributes: [this.binAttr],
            };
            fetch("/api/visualizations", {
                body: JSON.stringify(post),
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
            }).then(() => {
              this.$emit("posted-histogram");
            }, () => {
            });
        },
    },
    template: `
    <div>
      <label>Attribute:</label>
      <select v-model="binAttr">
        <option v-for="attr in attributes" v-bind:value="attr">
          {{ attr.name }}
        </option>
      </select>
      <br>
      <button v-on:click="postHistogram()">Submit</button>
    </div>
    `
});
Vue.component("scatterplot-definition-panel", {
    props: ['name', 'datasetLoc', 'attributes'],
    data: function () {
        return {xAxis: null, yAxis: null};
    },
    methods: {
        postScatterPlot: function () {
            var post = {
                name: this.name,
                dataset: this.datasetLoc,
                style: "scatterplot",
                attributes: [this.xAxis, this.yAxis]
            };
            fetch("/api/visualizations", {
                body: JSON.stringify(post),
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
            }).then(() => {
              this.$emit("posted-scatter");
            }, () => {
            });
        }
    },
    template: `
    <div>
      <label>X Axis:</label>
      <select v-model="xAxis">
        <option v-for="attr in attributes" v-bind:value="attr">
          {{ attr.name }}
        </option>
      </select>
      <br>
      <label>Y Axis:</label>
      <select v-model="yAxis">
        <option v-for="attr in attributes" v-bind:value="attr">
          {{ attr.name }}
        </option>
      </select>
      <br>
      <button v-on:click="postScatterPlot()">Submit</button>
    </div>
    `
})
Vue.component("visualization-definition-panel", {
    props: ['dataset'],
    methods: {
        propagatePost: function(attributes) {
          this.$emit("posted-viz");
        },
        filter_arithmetic: function(attributes) {
            const types = new Set(["integer", "floating-point"]);
            return attributes.filter(attr => types.has(attr.type));
        },
        filter_countable: function(attributes) {
            const types = new Set(["integer","enumerated","arbitrary"]);
            return attributes.filter(attr => types.has(attr.type));
        },
        attributes: function() {
            return this.dataset.definition.attributes;
        }
    },
    data: function () {
        return {
            style: null,
            name: null,
            styleChoices: [
                {"name": "Scatter Plot", value: "scatterplot"},
                {"name": "Histogram", value: "histogram"},
                {"name": "Series Plot", value: "series"}
            ],
        }
    },
    template: `
    <div class="card mb-3" v-if="dataset !== null">
      <div class="card-header">
        Define a Visualization
      </div>
      <div class="card-body">
        <label>Visualization Name:</label>
        <input v-model="name">
        <br>
        <label>Style:</label>
        <select v-model="style">
          <option v-for="choice in styleChoices" v-bind:value="choice.value">
            {{ choice.name }}
          </option>
        </select>
        <div v-if="style=='scatterplot'">
          <scatterplot-definition-panel
              v-on:posted-scatter="propagatePost"
              v-bind:name="name"
              v-bind:datasetLoc="this.dataset.location"
              v-bind:attributes="filter_arithmetic(attributes())">
          </scatterplot-definition-panel>
        </div>
        <div v-else-if="style=='histogram'">
          <histogram-definition-panel
              v-on:posted-histogram="propagatePost"
              v-bind:name="name"
              v-bind:datasetLoc="this.dataset.location"
              v-bind:attributes="filter_countable(attributes())">
          </histogram-definition-panel>
        </div>
        <div v-else-if="style=='series'">
          <series-definition-panel
              v-on:posted-series="propagatePost"
              v-bind:name="name"
              v-bind:datasetLoc="this.dataset.location"
              v-bind:attributes="filter_arithmetic(attributes())">
          </series-definition-panel>
        </div>
        <div v-else>
        </div>
      </div>
    </div>
    `
});
const visualize_Defn_DataDefn_component = new Vue({
  el: "#visualize_Defn_DataDefn",
  data: {
    datasets: null,
    visDatasets: null,
    location: null,
    selectedDataset: null,
    selChartType: "",
    visName: "",
    selAttribTwo: null,
    selAttribOne: null,
    chart_types: [ "histogram", "scatter", "series"]
  },
  methods: {
    refreshVisualizations: function () {
      this.getVisData();
    },
    log(status){
      var error = document.getElementById("error");
      console.log(status);
      if (typeof status.statusText != 'undefined')
        error.innerText = status.statusText;
      else
        error.innerText = status;
    },
    createSampleObject(){
            
      if(!this.visName){
        this.log("Please provide a dataset name for the sample attribute.");
        return null;
      }

      var bailBadDatasetName = false;
      if(this.visDatasets){
        this.visDatasets.forEach(function(t) {
          if(t.name === this.visName){
            bailBadDatasetName = true;
          }
        }); 
      }

      if(bailBadDatasetName){        
        this.log("Please provide a unique visualization name. This one already exists.");
        return;
      }
      
      if(!this.selAttribOne){
        this.log("Please choose the first attribute.");
        return;
      }

      if(this.selChartType == "scatter" && !this.selAttribTwo){      
        this.log("Please choose the second attribute.");
        return;
      }

      var attribObj1 = null;
      var attribObj2 = null;
      var found = null;
      for (var i in this.selectedDataset.definition.attributes) {
        if(this.selectedDataset.definition.attributes[i].name === this.selAttribOne){
          attribObj1 = this.selectedDataset.definition.attributes[i];
        }

        if(this.selectedDataset.definition.attributes[i].name === this.selAttribTwo){
          attribObj2 = this.selectedDataset.definition.attributes[i];
        }
      }

      if(this.selChartType == "scatter"){
        this.postdataset({ "name" : this.visName, "location": this.location,  "attributes" : [attribObj1, attribObj2] });
      } else {
        this.postdataset({ "name" : this.visName, "location": this.location,  "attributes" : [attribObj1] });
      } 
      
      
      return null;        
    },
    addVisualization(){
      var inputObj = this.createSampleObject();

      if(inputObj != null) {
        // We've got valid inputs, add the sample...
        
        if(!this.newAttributes){
          this.newAttributes = [];
        }
        
        this.newAttributes.push(inputObj);
      }
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
    getVisData(){
      fetch("/api/visualizations", {
        method: "GET",
        headers: {
          "Accept": "application/json"
        },
      })
      .then(response => response.json())
      .then((data) => {
        this.visDatasets = data;
      })
    },
    getDataset(location, i) {
      this.location = location;
      fetch(location, {
        method: "GET"
      })
      .then(response => response.json())
      .then((data) => {
        this.selectedDataset = data;
        this.selectedDataset.location = location;
      })        
    },
    postdataset(dataset) {
      function handleErrors(response) {
        if (!response.ok) {
            throw Error(response.statusText);
        }
        return response;
      }

        console.log(JSON.stringify(dataset.definition));

      fetch("/api/visualizations", {
        body: JSON.stringify(dataset.definition),
        method: "POST",
        headers: {
          "Accept": "application/json"
        },
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
      this.selAttrib = null;
      this.selChartType = "",
      this.visName = "",
      this.selAttrib2 = null,
      this.selAttrib1 = null,

      this.getData();
    }
  },
  mounted() {
    this.getData();
    this.getVisData();
  },
  template: ` 
  <div>

    <div v-if="datasets !== null" class="card mb-3">
      <div class="card mb-3">
        <div class="card-header">
          <i class="fa fa-table"></i>Choose existing dataset to visualize:
        </div>
        <div class="card-body">
          <div class="table-responsive">        
            <table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">
              <thead>
                <tr>
                  <th>Available Datasets</th>
                </tr>
              </thead>
            <tbody>
              <tr v-for="dataset, i in datasets">
                <button class="btn btn-link" v-on:click="getDataset(dataset.location, i)">{{dataset.name}}</button>
              </tr>
            </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div v-if="selectedDataset !== null">
      <visualization-definition-panel
        v-on:posted-viz="refreshVisualizations"
        v-bind:dataset="selectedDataset"/>
    </div>

    <div v-if="visDatasets !== null">
      <div class="card mb-3">
      <div class="card-header">
        <i class="fa fa-table"></i>Existing Visualizations</div>
      <div class="card-body">
        <div class="table-responsive">        
          <table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">
            <tbody>
              <tr v-for="dataset, i in visDatasets">
                <p>{{dataset.name}}</p>
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
