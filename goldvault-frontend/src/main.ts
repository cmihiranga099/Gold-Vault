import { Component } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app';  // ← change 'App' to 'AppComponent'

bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));