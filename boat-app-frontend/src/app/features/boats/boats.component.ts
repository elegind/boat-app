import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Boat } from '../../shared/models/boat.model';
import { BoatService } from '../../core/services/boat.service';
import { BoatCardComponent } from './components/boat-card/boat-card.component';
import { BoatDetailDialogComponent } from './components/boat-detail-dialog/boat-detail-dialog.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header';
import { TranslationService } from '../../core/services/translation.service';

@Component({
  selector: 'app-boats',
  standalone: true,
  imports: [
    BoatCardComponent,
    // BoatDetailDialogComponent is opened dynamically via MatDialog — not used in template
    PageHeaderComponent,
    MatPaginatorModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './boats.component.html',
  styleUrl: './boats.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoatsComponent implements OnInit {
  private readonly boatService = inject(BoatService);
  private readonly dialog = inject(MatDialog);
  protected readonly t = inject(TranslationService).t;

  protected readonly boats = signal<Boat[]>([]);
  protected readonly totalElements = signal<number>(0);
  protected readonly currentPage = signal<number>(0);
  protected readonly pageSize = signal<number>(5);
  protected readonly isLoading = signal<boolean>(false);

  readonly pageSizeOptions = [5, 10];

  ngOnInit(): void {
    this.loadBoats();
  }

  protected loadBoats(): void {
    this.isLoading.set(true);
    this.boatService.getBoats(this.currentPage(), this.pageSize()).subscribe({
      next: (page) => {
        this.boats.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load boats', err);
        this.isLoading.set(false);
      },
    });
  }

  protected onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadBoats();
  }

  openDetail(boat: Boat): void {
    this.dialog.open(BoatDetailDialogComponent, {
      data: { boat },
      width: '400px',
    });
  }
}



