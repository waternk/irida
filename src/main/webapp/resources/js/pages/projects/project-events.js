import angular from "angular";
import { EventsModule } from "../../modules/events/events";

const app = angular.module("irida");
app.requires.push(EventsModule);
