import React from 'react';
import {Chart} from "chart.js";

Chart.defaults.global.defaultFontColor = 'white'
Chart.defaults.global.legend.display = false
Chart.defaults.scale.ticks.beginAtZero = true;
Chart.defaults.scale.ticks.autoSkip = false;
Chart.defaults.scale.ticks.callback = (value) => {
    if (value.length <= 20) return value;
    return value.substring(0, 18) + '\u2026'; // If truncated, add triple dot
};

/**
 * @param data
 * @param type (horizontalBar or bar)
 * @param triggerUpdate
 */
class BarChart extends React.Component {
    constructor(props) {
        super(props);
        this.chartRef = React.createRef();
    }

    componentDidMount() {
        if (!this.props.data) return;

        this.myChart = new Chart(this.chartRef.current, {
            type: this.props.type,
            data: {
                labels: this.props.data.map(d => d.key),
                datasets: [{
                    data: this.props.data.map(d => d.value),
                    backgroundColor: "#FFFFFF",
                    maxBarThickness: 40
                }]
            }
        });
    }

    render() {
        if (this.myChart) {
            // The chart exists, we must update data.
            this.myChart.data.labels = this.props.data.map(d => d.key);
            this.myChart.data.datasets[0].data = this.props.data.map(d => d.value);
            this.myChart.update();
        }
        if (!this.props.data) return null;
        return (
            <canvas height={this.props.height} width={this.props.width} ref={this.chartRef}/>
        );
    }
}

export default BarChart;